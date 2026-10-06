import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
} from "react";
import {
    apiFetch,
    readJsonResponse,
    refreshCsrfToken,
} from "../api/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [authLoading, setAuthLoading] =
        useState(true);

    useEffect(() => {
        const initializeAuth = async () => {
            try {
                await refreshCsrfToken();

                const response = await apiFetch(
                    "/auth/me"
                );

                if (!response.ok) {
                    setUser(null);
                    return;
                }

                const data = await response.json();

                setUser({
                    userId: data.userId,
                    email: data.email,
                });
            } catch {
                setUser(null);
            } finally {
                setAuthLoading(false);
            }
        };

        initializeAuth();
    }, []);

    const login = async ({ email, password }) => {
        const response = await apiFetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                email,
                password,
            }),
        });

        const data = await readJsonResponse(response);

        if (!response.ok) {
            throw new Error(
                data?.error ??
                `로그인에 실패했습니다. (${response.status})`
            );
        }

        if (!data) {
            throw new Error("로그인 응답이 비어 있습니다.");
        }

        setUser({
            userId: data.userId,
            email: data.email,
        });

        await refreshCsrfToken();

        return data;
    };

    const logout = async () => {
        try {
            await apiFetch("/auth/logout", {
                method: "POST",
            });
        } finally {
            setUser(null);
            await refreshCsrfToken();
        }
    };

    const value = useMemo(
        () => ({
            userId: user?.userId ?? null,
            email: user?.email ?? null,
            isLoggedIn: Boolean(user),
            authLoading,
            login,
            logout,
        }),
        [user, authLoading]
    );

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error(
            "useAuth는 AuthProvider 내부에서 사용해야 합니다."
        );
    }

    return context;
}