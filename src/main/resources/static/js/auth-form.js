(function () {
  function initAuthForm(form) {
    var submitButton = form.querySelector(".btn-submit");
    var toggleButton = form.querySelector("[data-password-toggle]");
    var passwordFieldId = toggleButton ? toggleButton.getAttribute("data-password-input") : null;
    var passwordField = passwordFieldId ? document.getElementById(passwordFieldId) : null;

    if (toggleButton && passwordField) {
      toggleButton.addEventListener("click", function () {
        var isVisible = passwordField.type === "text";
        passwordField.type = isVisible ? "password" : "text";
        toggleButton.classList.toggle("is-visible", !isVisible);
      });
    }

    form.addEventListener("submit", function () {
      if (submitButton) {
        submitButton.classList.add("loading");
        submitButton.disabled = true;
      }
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var forms = document.querySelectorAll("[data-auth-form]");
    forms.forEach(initAuthForm);
  });
})();
