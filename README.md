```javascript
cordova.plugins.VideoPickerPlugin.pickVideo(function(videoPath) {
      alert('Selected video path: ' + videoPath);
}, function(error) {

        alert('Error picking video: ' + error);
});
