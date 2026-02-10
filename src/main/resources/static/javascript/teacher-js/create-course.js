	const courseType = document.getElementById("courseType");
    const priceGroup = document.getElementById("priceGroup");
    const priceInput = document.getElementById("priceInput");

    function togglePrice() {
        if (courseType.value === "paid") {
            priceGroup.classList.remove("hidden");
        } else {
            priceGroup.classList.add("hidden");
            priceInput.value = ""; // clear price if free
        }
    }

    // run on page load (important for edit mode)
    togglePrice();

    // run when user changes type
    courseType.addEventListener("change", togglePrice);

const thumbnailInput = document.getElementById("thumbnailInput");
const thumbnailPreview = document.getElementById("thumbnailPreview");

// Click UI box → open file dialog
thumbnailPreview.addEventListener("click", () => {
    thumbnailInput.click();
});

// Show preview after selection
thumbnailInput.addEventListener("change", () => {
    const file = thumbnailInput.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
        thumbnailPreview.innerHTML = `<img src="${reader.result}" alt="Thumbnail">`;
    };
    reader.readAsDataURL(file);
});


const toggleBtn = document.getElementById("sidebarToggle");
const sidebar = document.querySelector(".sidebar");

const body = document.body;

toggleBtn.addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
    body.classList.toggle("sidebar-collapsed");
});


const profileToggle = document.getElementById("profileToggle");
const profileDropdown = document.getElementById("profileDropdown");

if (profileToggle) {
    profileToggle.addEventListener("click", () => {
        profileDropdown.classList.toggle("show");
    });

    document.addEventListener("click", (e) => {
        if (!profileToggle.contains(e.target)) {
            profileDropdown.classList.remove("show");
        }
    });
}

            let moduleCount = 0;

            /* ADD MODULE */
            function addModule() {
                moduleCount++;
                const module = document.createElement("div");
                module.className = "module";
                module.innerHTML = `
        <div class="module-header">
            <input placeholder="Module title" style="border:none;width:70%">
            <button class="btn btn-outline" onclick="addLesson(${moduleCount})">+ Lesson</button>
        </div>
        <div class="lessons" id="lessons-${moduleCount}"></div>
    `;
                document.getElementById("modulesContainer").appendChild(module);
            }

            /* ADD LESSON */
            function addLesson(id) {
                const lessons = document.getElementById(`lessons-${id}`);
                const lesson = document.createElement("div");
                lesson.className = "lesson";
                lesson.innerHTML = `
        <div class="lesson-header">
            <input placeholder="Lesson title">
            <select onchange="lessonTypeChange(this)">
                <option value="video">Video</option>
                <option value="notes">Notes</option>
                <option value="quiz">Quiz</option>
            </select>
            <button onclick="removeItem(this)">🗑</button>
        </div>
        <div class="lesson-content"></div>
    `;
                lessons.appendChild(lesson);
            }

            /* CHANGE TYPE */
            function lessonTypeChange(select) {
                const content = select.closest(".lesson").querySelector(".lesson-content");
                if (select.value === "video") {
                    content.innerHTML = `<input type="file"><label><input type="checkbox"> Free Preview</label>`;
                }
                if (select.value === "notes") {
                    content.innerHTML = `<input type="file" accept="application/pdf">`;
                }
                if (select.value === "quiz") {
                    content.innerHTML = `<button class="btn btn-outline" onclick="addQuizQuestion(this)">+ Add Question</button><div class="quiz-questions"></div>`;
                }
            }

            /* ADD QUIZ */
            function addQuizQuestion(btn) {
                const container = btn.nextElementSibling;
                const q = document.createElement("div");
                q.className = "quiz-question";
                q.innerHTML = `
        <input placeholder="Question">
        <div class="quiz-options">
            <input placeholder="Option A">
            <input placeholder="Option B">
            <input placeholder="Option C">
            <input placeholder="Option D">
        </div>
        <select>
            <option>Correct Answer</option>
            <option>A</option>
            <option>B</option>
            <option>C</option>
            <option>D</option>
        </select>
        <button onclick="removeItem(this)">Remove</button>
    `;
                container.appendChild(q);
            }

            function removeItem(btn) {
                btn.closest(".lesson, .quiz-question").remove();
            }
        