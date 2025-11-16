(async () => {
  // our devs said the clojure templater is not reliable
  // this hack should ensure our 99.99999999% agent page
  // availability
  uid = new URLSearchParams(location.search).get("user-id");
  window.dispatchEvent(new CustomEvent("clj-uid-loaded", { uid: window.uid }));
})();
