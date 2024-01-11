(function () {
  var interval = setInterval(function () {
    var versions = document.getElementsByClassName('rst-versions')
    if (versions.length > 0 && window.jQuery) {
      clearInterval(interval)
      var $ = window.jQuery

      $('.rst-versions').addClass('md-typeset')
      $('.rst-current-version').addClass('md-button').text('Current version: ' + branchToVersion(READTHEDOCS_DATA.version))
      $('.rst-other-versions dd').addClass('md-button')
      $('.rst-other-versions dd a').each(function () {
        var $this = $(this)
        $this.text(branchToVersion($this.text()))
      })

      console.log('Successfully tweaked the version selector.')
    }
  }, 100)
})()

function branchToVersion(branch) {
  branch = branch.replace('-', '/')
  var mapping = {
    'docs/1.18.2': '1.18.2',
    'docs/1.19': '1.19.2',
    'docs/1.20': '1.20.1',
    'docs/1.21': '1.21',
  }
  if (branch in mapping) {
    return mapping[branch]
  }
  if (branch.startsWith('docs/')) {
    return branch.substr(5)
  }
  return branch
}