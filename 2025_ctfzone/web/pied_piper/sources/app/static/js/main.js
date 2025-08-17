function onSubmit(token) {
  const formData = new FormData();
  const audioFile = document.getElementById("audio-file").files[0];
  const title = document.getElementById("song-title").value;
  const artist = document.getElementById("song-artist").value;

  formData.append("audio_file", audioFile);
  formData.append("title", title);
  formData.append("artist", artist);
  formData.append("g-recaptcha-response", token);

  fetch("/song/upload", {
    method: "POST",
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        return response.json().then((err) => {
          throw new Error(err.error || "Upload failed");
        });
      }
      return response;
    })
    .then((data) => {
      const successMsg = `
            <h3>Your song has been successfully uploaded!</h3>
            <p>It is currently under review by our moderation team.</p>
            <p>If approved, it will be added to our public database within 24 hours.</p>
            <p>Thank you for contributing to Pied Piper!"</p>
        `;
      showModal(successMsg, true);

      document.getElementById("upload-form").reset();
      document.querySelector(".upload-text").textContent =
        "Choose audio file or drag here";
      document.querySelector(".upload-icon").textContent = "+";
      document.querySelector(".upload-icon").style.color = "";

      grecaptcha.reset();
    })
    .catch((error) => {
      const errorMsg = `
            <h3>Error!</h3>
            <p>${error.message}</p>
            <p>Please try again or contact support.</p>
        `;
      showModal(errorMsg, false);

      grecaptcha.reset();
    });
}

function showModal(message, isSuccess) {
  let modalMessage = document.getElementById("modal-message");
  let modal = document.getElementById("response-modal");

  modalMessage.innerHTML = message;
  modalMessage.className = isSuccess ? "success-message" : "error-message";
  modal.style.display = "block";
}

document.addEventListener("DOMContentLoaded", function () {
  const fileInput = document.getElementById("audio-file");
  const uploadLabel = document.querySelector(".upload-label");

  fileInput.addEventListener("change", function (e) {
    if (this.files.length) {
      const fileName = this.files[0].name;
      const uploadText = uploadLabel.querySelector(".upload-text");
      uploadText.textContent = fileName;

      const uploadIcon = uploadLabel.querySelector(".upload-icon");
      uploadIcon.textContent = "✓";
      uploadIcon.style.color = "#4caf50";
    }
  });

  uploadLabel.addEventListener("dragover", function (e) {
    e.preventDefault();
    this.style.backgroundColor = "#e8f5e9";
  });

  uploadLabel.addEventListener("dragleave", function (e) {
    e.preventDefault();
    this.style.backgroundColor = "";
  });

  uploadLabel.addEventListener("drop", function (e) {
    e.preventDefault();
    this.style.backgroundColor = "";
    fileInput.files = e.dataTransfer.files;

    const event = new Event("change");
    fileInput.dispatchEvent(event);
  });

  const modal = document.getElementById("response-modal");
  const closeModal = document.querySelector(".close-modal");

  closeModal.onclick = function () {
    modal.style.display = "none";
  };

  window.onclick = function (event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };

  document
    .getElementById("upload-form")
    .addEventListener("submit", function (e) {
      e.preventDefault();

      const audioFile = document.getElementById("audio-file").files[0];
      const title = document.getElementById("song-title").value;
      const artist = document.getElementById("song-artist").value;

      if (!audioFile || !title || !artist) {
        const errorMsg = `
            <h3>Error!</h3>
            <p>Please fill all required fields</p>
        `;
        showModal(errorMsg, false);
        return;
      }

      grecaptcha.execute();
    });

  const viewButtons = document.querySelectorAll(".view-btn");
  viewButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const songId = this.getAttribute("data-id");
      document.location = "/song/" + songId;
    });
  });

  const searchInput = document.querySelector(".search-box input");
  const searchButton = document.querySelector(".search-btn");

  function performSearch() {
    const query = searchInput.value.toLowerCase();
    alert(
      `Would normally search for: "${query}"\nIn a real app, this would filter the songs or make an API call.`
    );
  }

  searchButton.addEventListener("click", performSearch);
  searchInput.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
      performSearch();
    }
  });

  document
    .querySelector(".collapsible-title")
    .addEventListener("click", function () {
      this.classList.toggle("active");
      const content = this.nextElementSibling;
      content.classList.toggle("expanded");

      if (content.classList.contains("expanded")) {
        setTimeout(() => {
          content.scrollIntoView({ behavior: "smooth", block: "nearest" });
        }, 100);
      }
    });
});

function setupModerationPage(songId) {
  let currentSongData = {};

  const approveBtn = document.getElementById("approve-btn");
  const editBtn = document.getElementById("edit-btn");
  const saveBtn = document.getElementById("save-btn");
  const cancelBtn = document.getElementById("cancel-btn");
  const formContainer = document.getElementById("edit-form");
  const viewContainer = document.getElementById("song-view");
  const statusBadge = document.getElementById("status-badge");
  const songTitle = document.getElementById("song-title");
  const songArtist = document.getElementById("song-artist");
  const lyricsContainer = document.getElementById("lyrics-container");

  function loadSongData() {
    fetch(`/admin/api/song/${songId}`)
      .then((response) => response.json())
      .then((data) => {
        currentSongData = data.song;
        updateSongView();
      })
      .catch((error) => console.error("Error:", error));
  }

  function updateSongView() {
    songTitle.textContent = currentSongData.title;
    songArtist.textContent = currentSongData.artist;
    lyricsContainer.innerHTML = currentSongData.lyrics;

    if (currentSongData.is_moderated) {
      statusBadge.textContent = "Approved";
      statusBadge.className = "status-approved";
      approveBtn.textContent = "Decline";
      approveBtn.dataset.status = "true";
      approveBtn.className = "moderation-btn status-approved";
    } else {
      statusBadge.textContent = "Pending";
      statusBadge.className = "status-pending";
      approveBtn.textContent = "Approve";
      approveBtn.dataset.status = "false";
      approveBtn.className = "moderation-btn status-pending";
    }
  }

  approveBtn.addEventListener("click", function () {
    const isApproved = this.dataset.status === "true";
    const endpoint = isApproved ? "decline" : "approve";

    fetch(`/admin/api/song/${songId}/${endpoint}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          currentSongData.is_moderated = !isApproved;
          updateSongView();
        }
      });
  });

  editBtn.addEventListener("click", function () {
    viewContainer.style.display = "none";
    formContainer.style.display = "block";

    document.getElementById("edit-title").value = currentSongData.title;
    document.getElementById("edit-artist").value = currentSongData.artist;
    document.getElementById("edit-lyrics").value = currentSongData.lyrics;
  });

  cancelBtn.addEventListener("click", function () {
    viewContainer.style.display = "block";
    formContainer.style.display = "none";
  });

  saveBtn.addEventListener("click", function () {
    const formData = {
      title: document.getElementById("edit-title").value,
      artist: document.getElementById("edit-artist").value,
      lyrics: document.getElementById("edit-lyrics").value,
    };

    fetch(`/admin/api/song/${songId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(formData),
    })
      .then((response) => response.json())
      .then((data) => {
        if (data.success) {
          currentSongData = {
            ...currentSongData,
            ...formData,
          };

          viewContainer.style.display = "block";
          formContainer.style.display = "none";
          updateSongView();
        }
      });
  });

  loadSongData();
}
