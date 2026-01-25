// Run on load and on resize
function adjustSidebar() {
    const checkbox = document.getElementById('checkbox-input');
    if (window.innerWidth < 768) {
        checkbox.checked = false;
    } else {
        checkbox.checked = true;
    }
}
window.addEventListener('resize', adjustSidebar);
adjustSidebar(); // Initial check