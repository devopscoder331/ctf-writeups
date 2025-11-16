(async () => {
  let { uid } = document.currentScript.dataset;

  // load the fallback uid
  if (uid) {
    window.uid = uid;
  } else {
    uid = window.uid;
  }

  // uid must be available
  console.log(uid);

  userData = await (await fetch(`/users/${uid}`)).json();
  console.log(userData);
})();
