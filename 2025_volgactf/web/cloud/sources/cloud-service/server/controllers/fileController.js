const File = require('../models/File');
const Folder = require('../models/Folder');
const fs = require('fs').promises;
const path = require('path');

exports.uploadFile = async (req, res) => {
  try {
    const { originalname, filename, size, mimetype, path: filePath } = req.file;
    const { folderId } = req.body;

    if (folderId) {
      const folder = await Folder.findOne({ _id: folderId, user: req.session.userId });
      if (!folder) {
        return res.status(404).json({ message: 'Folder not found' });
      }
    }

    const file = new File({
      filename,
      originalName: originalname,
      path: filePath,
      size,
      mimetype,
      user: req.session.userId,
      parent: folderId || null
    });

    await file.save();
    res.status(201).json({ message: 'File uploaded successfully', file });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.createFolder = async (req, res) => {
  try {
    const { name, parentId } = req.body;
    
    let parentPath = '/';
    if (parentId) {
      const parentFolder = await Folder.findOne({ 
        _id: parentId,
        user: req.session.userId
      });
      
      if (!parentFolder) {
        return res.status(404).json({ message: 'Parent folder not found' });
      }
      
      parentPath = path.join(parentFolder.path, parentFolder.name);
    }
    
    const folder = new Folder({
      name,
      user: req.session.userId,
      parent: parentId || null,
      path: parentPath
    });
    
    await folder.save();
    res.status(201).json({ message: 'Folder created successfully', folder });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.getItems = async (req, res) => {
  try {
    let { folderId } = req.query;
    if (typeof folderId !== 'string') folderId = "";

    const query = { user: req.session.userId };
    
    if (folderId) {
      query.parent = folderId;
    } else {
      query.parent = null;
    }
    
    const [files, folders] = await Promise.all([
      File.find({ ...query, isFolder: false }),
      Folder.find(query)
    ]);
    
    res.json({ files, folders });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.deleteItem = async (req, res) => {
  try {
    const { id, type } = req.params;
    
    if (type !== 'file' && type !== 'folder') {
      return res.status(400).json({ message: 'Invalid item type. Must be either "file" or "folder".' });
    }
    
    if (!id.match(/^[0-9a-fA-F]{24}$/)) {
      return res.status(400).json({ message: 'Invalid ID format' });
    }
    
    if (type === 'file') {
      const file = await File.findOne({ _id: id, user: req.session.userId });
      
      if (!file) {
        return res.status(404).json({ message: 'File not found' });
      }
      
      await fs.unlink(file.path);
      await File.findByIdAndDelete(id);
      
      res.json({ message: 'File deleted successfully' });
    } else if (type === 'folder') {
      const folder = await Folder.findOne({ _id: id, user: req.session.userId });
      
      if (!folder) {
        return res.status(404).json({ message: 'Folder not found' });
      }
      
      const allFolders = await Folder.find({ 
        path: { $regex: `^${folder.path}/${folder.name}` },
        user: req.session.userId
      });
      
      const folderIds = [id, ...allFolders.map(f => f._id)];
      
      const files = await File.find({ 
        parent: { $in: folderIds },
        user: req.session.userId 
      });
      
      for (const file of files) {
        await fs.unlink(file.path).catch(e => console.error(`Failed to delete file ${file.path}:`, e));
      }
      
      await Promise.all([
        File.deleteMany({ parent: { $in: folderIds }, user: req.session.userId }),
        Folder.deleteMany({ _id: { $in: folderIds }, user: req.session.userId })
      ]);
      
      res.json({ message: 'Folder and all its contents deleted successfully' });
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.shareFile = async (req, res) => {
  try {
    const { id } = req.params;
    const { expiryHours } = req.body;
    
    if (!id.match(/^[0-9a-fA-F]{24}$/)) {
      return res.status(400).json({ message: 'Invalid ID format' });
    }
    
    const file = await File.findOne({ _id: id, user: req.session.userId });
    
    if (!file) {
      return res.status(404).json({ message: 'File not found' });
    }
    
    const shareToken = file.generateShareToken(expiryHours || 24);
    await file.save();
    
    const baseUrl = `${req.protocol}://${process.env.PREVIEW_DOMAIN}`;
    const shareUrl = `${baseUrl}/preview/shared/${shareToken}`;
    
    res.json({ 
      message: 'Share link generated successfully', 
      shareUrl,
      expiresAt: file.shareExpiry
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.revokeShareLink = async (req, res) => {
  try {
    const { id } = req.params;
    
    if (!id.match(/^[0-9a-fA-F]{24}$/)) {
      return res.status(400).json({ message: 'Invalid ID format' });
    }
    
    const file = await File.findOne({ _id: id, user: req.session.userId });
    
    if (!file) {
      return res.status(404).json({ message: 'File not found' });
    }
    
    file.revokeShare();
    await file.save();
    
    res.json({ message: 'Share link revoked successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.getSharedFile = async (req, res) => {
  try {
    const { token } = req.params;
    const download = req.query.download === 'true';
  
    const file = await File.findOne({ shareToken: token });
    
    if (!file || !file.isShareValid()) {
      return res.status(404).render('shared/error', { 
        message: 'Invalid or expired share link' 
      });
    }
    
    if (download) {
      res.setHeader('Content-Disposition', `inline; filename="${file.originalName}"`);
      res.setHeader('Content-Type', file.mimetype);
      
      const fileContent = await fs.readFile(file.path);
      return res.send(fileContent);
    }
    
    const baseUrl = `${req.protocol}://${process.env.PREVIEW_DOMAIN}`;
    const downloadUrl = `${baseUrl}/preview/shared/${token}?download=true`;
    
    res.render('shared/file', {
      file,
      downloadUrl
    });
  } catch (error) {
    res.status(500).render('shared/error', { 
      message: 'An error occurred while trying to access the shared file.' 
    });
  }
};