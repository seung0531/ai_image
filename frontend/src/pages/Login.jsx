import { useState } from "react";
import {
    Link,
    Navigate,
    useNavigate,
} from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Login() {
    const navigate = useNavigate();
    const { login, isLoggedIn } = useAuth();

    const [form, setForm] = useState({
        email: "",
        password: "",
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    if (isLoggedIn) {
        return <Navigate to="/" replace />;
    }

    const handleChange = (event) => {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");

        try {
            setLoading(true);

            await login({
                email: form.email.trim(),
                password: form.password,
            });

            navigate("/", { replace: true });
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>로그인</h1>

                <label htmlFor="email">이메일</label>
                <input
                    id="email"
                    name="email"
                    type="email"
                    value={form.email}
                    onChange={handleChange}
                    placeholder="example@email.com"
                    autoComplete="email"
                    required
                />

                <label htmlFor="password">비밀번호</label>
                <input
                    id="password"
                    name="password"
                    type="password"
                    value={form.password}
                    onChange={handleChange}
                    placeholder="비밀번호 입력"
                    autoComplete="current-password"
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "로그인 중..." : "로그인"}
                </button>

                {error && (
                    <p className="error-message">{error}</p>
                )}

                <p className="auth-link">
                    계정이 없나요?{" "}
                    <Link to="/register">회원가입</Link>
                </p>
            </form>
        </main>
    );
}

export default Login;