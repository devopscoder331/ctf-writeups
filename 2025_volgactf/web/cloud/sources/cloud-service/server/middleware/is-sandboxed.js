
const isSandboxed = (req, res, next) => {
    if (req.hostname === process.env.PREVIEW_DOMAIN) {
        next();
    } else {
        res.status(403).json({ message: 'Wrong domain' });
    }
}

module.exports = isSandboxed;