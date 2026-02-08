document.addEventListener("DOMContentLoaded", () => {

    /* ===== SIDEBAR TOGGLE ===== */
    const toggleBtn = document.getElementById("sidebarToggle");
    const sidebar = document.querySelector(".sidebar");

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener("click", () => {
            sidebar.classList.toggle("collapsed");
        });
    }

    /* ================= CREATE COURSE MODAL ================= */
    const courseModal = document.getElementById("createCourseModal");
    const openCourseBtn = document.querySelector(".primary-btn");

    if (courseModal && openCourseBtn) {
        const closeCourseBtn = courseModal.querySelector(".close-btn");
        const cancelCourseBtn = courseModal.querySelector(".cancel-btn");

        openCourseBtn.addEventListener("click", () => {
            courseModal.classList.add("show");
        });

        function closeCourseModal() {
            courseModal.classList.remove("show");
        }

        closeCourseBtn?.addEventListener("click", closeCourseModal);
        cancelCourseBtn?.addEventListener("click", closeCourseModal);

        window.addEventListener("click", (e) => {
            if (e.target === courseModal) closeCourseModal();
        });
    }

    /* ================= CHANGE PASSWORD MODAL ================= */
    const openPasswordBtn = document.getElementById("openPassword");
    const passwordModal = document.getElementById("passwordModal");

    if (openPasswordBtn && passwordModal) {
        const closePasswordBtn = passwordModal.querySelector(".close-btn");
        const cancelPasswordBtn = passwordModal.querySelector(".cancel-btn");

        openPasswordBtn.addEventListener("click", (e) => {
            e.preventDefault();
            passwordModal.classList.add("show");
        });

        function closePasswordModal() {
            passwordModal.classList.remove("show");
        }

        closePasswordBtn?.addEventListener("click", closePasswordModal);
        cancelPasswordBtn?.addEventListener("click", closePasswordModal);

        window.addEventListener("click", (e) => {
            if (e.target === passwordModal) closePasswordModal();
        });
    }

	/* ===== EMAIL MODAL ===== */
	  const openEmail = document.getElementById("openEmailModal");
	  const emailModal = document.getElementById("emailModal");
	  const closeEmail = document.getElementById("closeEmailModal");
	  const cancelEmail = document.getElementById("cancelEmail");

	  openEmail?.addEventListener("click", () => {
	    emailModal.classList.add("show");
	  });

	  function closeEmailModal() {
	    emailModal.classList.remove("show");
	  }

	  closeEmail?.addEventListener("click", closeEmailModal);
	  cancelEmail?.addEventListener("click", closeEmailModal);


	  /* ===== PHONE MODAL ===== */
	  const openPhone = document.getElementById("openPhoneModal");
	  const phoneModal = document.getElementById("phoneModal");
	  const closePhone = document.getElementById("closePhoneModal");
	  const cancelPhone = document.getElementById("cancelPhone");

	  openPhone?.addEventListener("click", () => {
	    phoneModal.classList.add("show");
	  });

	  function closePhoneModal() {
	    phoneModal.classList.remove("show");
	  }

	  closePhone?.addEventListener("click", closePhoneModal);
	  cancelPhone?.addEventListener("click", closePhoneModal);


});
