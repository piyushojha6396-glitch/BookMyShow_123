// Keep the UI deployable from any static host while allowing a local API override.
const API = window.BMS_API_URL || "http://localhost:8080/api";

function getAccessToken() {
    return localStorage.getItem("bms_token");
}

function unwrapPage(payload) {
    return Array.isArray(payload) ? payload : (payload?.content || []);
}

async function request(endpoint, options = {}) {
    const headers = { Accept: "application/json", ...(options.headers || {}) };
    const token = getAccessToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    if (options.body) headers["Content-Type"] = "application/json";

    let response;
    try {
        response = await fetch(`${API}${endpoint}`, { ...options, headers });
    } catch {
        throw new Error("The service is unavailable. Please check that the server is running.");
    }

    const text = await response.text();
    let payload = null;
    try { payload = text ? JSON.parse(text) : null; } catch { payload = text; }
    if (!response.ok) {
        if (response.status === 401) {
            localStorage.removeItem("bms_token");
            localStorage.removeItem("bms_user");
        }
        const fieldErrors = payload?.errors
            ?.map((error) => `${error.field}: ${error.message}`)
            .join(", ");
        const message = fieldErrors || payload?.message || payload?.error
            || (typeof payload === "string" ? payload : response.statusText);
        throw new Error(message || "Request failed");
    }
    return payload;
}

const apiGet = (endpoint) => request(endpoint);
const apiPost = (endpoint, data) => request(endpoint, { method: "POST", body: JSON.stringify(data) });
const apiPut = (endpoint, data) => request(endpoint, { method: "PUT", ...(data ? { body: JSON.stringify(data) } : {}) });
const apiPatch = (endpoint, data) => request(endpoint, { method: "PATCH", ...(data ? { body: JSON.stringify(data) } : {}) });
const apiDelete = (endpoint) => request(endpoint, { method: "DELETE" });

const UserAPI = {
    register: async (data) => {
        const response = await apiPost("/auth/register", data);
        return response;
    },
    login: async (data) => {
        const response = await apiPost("/auth/login", data);
        return response;
    },
    getAll: async () => unwrapPage(await apiGet("/users")),
    getById: (id) => apiGet(`/users/${id}`),
    me: () => apiGet("/users/me")
};

const CityAPI = {
    add: (data) => apiPost("/cities", data),
    getAll: () => apiGet("/cities"),
    getById: (id) => apiGet(`/cities/${id}`)
};

const MovieAPI = {
    add: (data) => apiPost("/movies", data),
    getAll: async () => unwrapPage(await apiGet("/movies?size=100")),
    getById: (id) => apiGet(`/movies/${id}`),
    search: (title) => apiGet(`/movies/search?title=${encodeURIComponent(title)}`),
    getByGenre: (genre) => apiGet(`/movies/genre/${encodeURIComponent(genre)}`),
    getByLanguage: (lang) => apiGet(`/movies/language/${encodeURIComponent(lang)}`),
    update: (id, data) => apiPut(`/movies/${id}`, data),
    delete: (id) => apiDelete(`/movies/${id}`)
};

const TheaterAPI = {
    add: (data) => apiPost("/theaters", data),
    getAll: async () => unwrapPage(await apiGet("/theaters?size=100")),
    getById: (id) => apiGet(`/theaters/${id}`),
    getByCity: (cityId) => apiGet(`/theaters/city/${cityId}`)
};

const ScreenAPI = {
    add: (data) => apiPost("/screens", data),
    getAll: async () => unwrapPage(await apiGet("/screens?size=100")),
    getById: (id) => apiGet(`/screens/${id}`),
    getByTheater: (theaterId) => apiGet(`/screens/theater/${theaterId}`)
};

const SeatAPI = {
    add: (data) => apiPost("/seats", data),
    getByScreen: (screenId) => apiGet(`/seats/screen/${screenId}`),
    getById: (id) => apiGet(`/seats/${id}`)
};

const ShowAPI = {
    add: (data) => apiPost("/shows", data),
    getAll: async () => unwrapPage(await apiGet("/shows?size=100")),
    getById: (id) => apiGet(`/shows/${id}`),
    getByMovie: (movieId, date, cityId) => {
        const params = new URLSearchParams();
        if (date) params.set("date", date);
        if (cityId) params.set("cityId", cityId);
        const query = params.toString();
        return apiGet(`/shows/movie/${movieId}${query ? `?${query}` : ""}`);
    },
    getByMovieAndDate: (movieId, date) => apiGet(`/shows/movie/${movieId}/date?date=${encodeURIComponent(date)}`)
};

const BookingAPI = {
    create: (data) => apiPost("/bookings", data),
    getById: (id) => apiGet(`/bookings/${id}`),
    getByUser: async (userId) => unwrapPage(await apiGet("/bookings/me?size=100")),
    cancel: (id) => apiPut(`/bookings/${id}/cancel`),
    getAvailableSeats: (showId) => apiGet(`/shows/${showId}/available-seats`)
};
