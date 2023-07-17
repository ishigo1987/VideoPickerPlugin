const exec = require('cordova/exec');

const VideoPickerPlugin = {
    pickVideo: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'VideoPickerPlugin', 'pickVideo', []);
    }
};

module.exports = VideoPickerPlugin;

