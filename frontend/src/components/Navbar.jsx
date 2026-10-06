import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Navbar() {
    const navigate = useNavigate();
    const { isLoggedIn, email, logout } = useAuth();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <nav className="navbar">
            <Link to="/">홈</Link>
            <Link to="/gallery">공개 갤러리</Link>
            {isLoggedIn ? (
                <>
                    <Link to="/my-images">내 이미지</Link>

                    <span className="navbar-email">{email}</span>

                    <button
                        type="button"
                        className="logout-button"
                        onClick={handleLogout}
                    >
                        로그아웃
                    </button>
                </>
            ) : (
                <>
                    <div className="navbar-spacer" />

                    <Link to="/register">회원가입</Link>
                    <Link to="/login">로그인</Link>
                </>
            )}
        </nav>
    );
}

export default Navbar;