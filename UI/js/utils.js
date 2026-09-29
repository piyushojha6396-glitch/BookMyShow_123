function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (char) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    }[char]));
}

function showToast(message, type = "info") {
    let container = document.querySelector(".toast-container");
    if (!container) {
        container = document.createElement("div");
        container.className = "toast-container";
        document.body.appendChild(container);
    }
    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3200);
}

function setLoggedInUser(authResponse) {
    const user = authResponse?.user || authResponse;
    if (authResponse?.accessToken) localStorage.setItem("bms_token", authResponse.accessToken);
    localStorage.setItem("bms_user", JSON.stringify(user));
    updateNavUser();
}

function getLoggedInUser() {
    try { return JSON.parse(localStorage.getItem("bms_user") || "null"); }
    catch { localStorage.removeItem("bms_user"); return null; }
}

function logout() {
    localStorage.removeItem("bms_token");
    localStorage.removeItem("bms_user");
    updateNavUser();
    showToast("You have been signed out", "success");
    if (window.location.pathname.includes("bookings") || window.location.pathname.includes("admin")) {
        setTimeout(() => { window.location.href = getBasePath() + "index.html"; }, 500);
    }
}

function updateNavUser() {
    const navUser = document.getElementById("nav-user");
    if (!navUser) return;
    const user = getLoggedInUser();
    if (user) {
        navUser.innerHTML = `<span class="nav-greeting">Hi, <strong>${escapeHtml(user.name)}</strong></span>
            <button class="btn btn-sm btn-outline" onclick="logout()">Sign out</button>`;
    } else {
        navUser.innerHTML = `<a href="${getPagePath("pages/login.html")}" class="btn btn-sm btn-outline">Log in</a>
            <a href="${getPagePath("pages/register.html")}" class="btn btn-sm btn-primary">Create account</a>`;
    }
}

function getPagePath(path) {
    return window.location.pathname.includes("/pages/") ? path.replace("pages/", "").replace("../", "") : path;
}

function getBasePath() {
    return window.location.pathname.includes("/pages/") ? "../" : "./";
}

function showLoading(containerId) {
    const element = document.getElementById(containerId);
    if (element) element.innerHTML = '<div class="loading-center"><div class="spinner"></div><span>Loading...</span></div>';
}

function showEmpty(containerId, message = "No data found") {
    const element = document.getElementById(containerId);
    if (element) element.innerHTML = `<div class="empty-state"><div class="icon">◌</div><p>${escapeHtml(message)}</p></div>`;
}

function openModal(modalId) {
    document.getElementById(modalId)?.classList.add("active");
    document.body.classList.add("modal-open");
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.remove("active");
    document.body.classList.remove("modal-open");
}

document.addEventListener("click", (event) => {
    if (event.target.classList.contains("modal-overlay")) closeModal(event.target.id);
});

document.addEventListener("DOMContentLoaded", updateNavUser);
