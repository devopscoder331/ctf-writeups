const track = (req, res, next) => {
    if (!req.session.track) {
        req.session.track = {
            ip: req.ip,
            userAgent: req.headers['user-agent']
        }
    }
    next();
}

module.exports = track;