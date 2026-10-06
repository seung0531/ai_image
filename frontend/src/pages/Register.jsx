import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiFetch } from "../api/api";

function Register() {
    const navigate = useNavigate();
    const { isLoggedIn } = useAuth();

    if (isLoggedIn) {
        return <Navigate to="/" replace />;
    }

    const [form, setForm] = useState({
        email: "",
        password: "",
        passwordConfirm: "",
    });

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setForm((previousForm) => ({
            ...previousForm,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (form.password !== form.passwordConfirm) {
            setError("비밀번호가 일치하지 않습니다.");
            return;
        }

        if (form.password.length < 8) {
            setError("비밀번호는 8자 이상이어야 합니다.");
            return;
        }

        try {
            setLoading(true);

            const response = await apiFetch(
                "/auth/register",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        email: form.email.trim(),
                        password: form.password,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.error ?? "회원가입에 실패했습니다."
                );
            }

            setMessage(data.message);

            setTimeout(() => {
                navigate("/login");
            }, 1000);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="auth-page">
            <form className="auth-form" onSubmit={handleSubmit}>
                <h1>회원가입</h1>

                <label htmlFor="email">이메일</label>
                <input
                    id="email"
                    name="email"
                    type="email"
                    value={form.email}
                    onChange={handleChange}
                    placeholder="example@email.com"
                    required
                />

                <label htmlFor="password">비밀번호</label>
                <input
                    id="password"
                    name="password"
                    type="password"
                    value={form.password}
                    onChange={handleChange}
                    placeholder="8자 이상 입력"
                    required
                />

                <label htmlFor="passwordConfirm">
                    비밀번호 확인
                </label>
                <input
                    id="passwordConfirm"
                    name="passwordConfirm"
                    type="password"
                    value={form.passwordConfirm}
                    onChange={handleChange}
                    placeholder="비밀번호 다시 입력"
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "가입 중..." : "회원가입"}
                </button>

                {message && (
                    <p className="success-message">{message}</p>
                )}

                {error && (
                    <p className="error-message">{error}</p>
                )}
            </form>
        </main>
    );
}

export default Register;