const API_URL = "http://localhost:8080/api";

let csrfToken = null;

export async function refreshCsrfToken() {
    const response = await fetch(`${API_URL}/auth/csrf`, {
        method: "GET",
        credentials: "include",
    });

    if (!response.ok) {
        throw new Error(
            `CSRF 토큰 요청 실패: ${response.status}`
        );
    }

    const data = await response.json();

    csrfToken = data.token;

    return csrfToken;
}

export async function apiFetch(path, options = {}) {
    const method = (options.method ?? "GET").toUpperCase();

    const unsafeMethods = [
        "POST",
        "PUT",
        "PATCH",
        "DELETE",
    ];

    const requiresCsrf =
        unsafeMethods.includes(method) &&
        path !== "/auth/login" &&
        path !== "/auth/register";

    if (requiresCsrf) {
        await refreshCsrfToken();
    }

    const headers = new Headers(
        options.headers ?? {}
    );

    if (requiresCsrf && csrfToken) {
        headers.set("X-XSRF-TOKEN", csrfToken);
    }

    return fetch(`${API_URL}${path}`, {
        ...options,
        method,
        headers,
        credentials: "include",
    });
}

export async function readJsonResponse(response) {
    const text = await response.text();

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        throw new Error(
            `올바르지 않은 서버 응답입니다. 상태 코드: ${response.status}`
        );
    }
}