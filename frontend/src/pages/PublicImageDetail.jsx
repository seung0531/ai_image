import { useEffect, useState } from "react";
import {
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    apiFetch,
    readJsonResponse,
} from "../api/api";

import { useAuth } from "../context/AuthContext";

function PublicImageDetail() {
    const { imageId } = useParams();
    const navigate = useNavigate();

    const {
        isLoggedIn,
    } = useAuth();

    const [image, setImage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadImage = async () => {
            try {
                setLoading(true);
                setError("");

                const response = await apiFetch(
                    `/image/public/${imageId}`
                );

                const data =
                    await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(
                        data?.error ??
                        data?.message ??
                        "이미지를 불러오지 못했습니다."
                    );
                }

                setImage(data);

            } catch (requestError) {
                setError(requestError.message);

            } finally {
                setLoading(false);
            }
        };

        loadImage();
    }, [imageId]);

    const handleLike = async () => {
        if (!isLoggedIn) {
            navigate("/login");
            return;
        }

        try {
            const response = await apiFetch(
                `/image/public/${imageId}/like`,
                {
                    method: "POST",
                }
            );

            const data =
                await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.error ??
                    "좋아요 처리에 실패했습니다."
                );
            }

            setImage((previous) => ({
                ...previous,
                liked: !previous.liked,
                likeCount: data.likeCount,
            }));

        } catch (requestError) {
            setError(requestError.message);
        }
    };

    if (loading) {
        return (
            <p className="page-message">
                이미지 불러오는 중...
            </p>
        );
    }

    if (error) {
        return (
            <p className="error-message">
                {error}
            </p>
        );
    }

    if (!image) {
        return null;
    }

    return (
        <main className="image-detail-page">

            <button
                type="button"
                className="back-button"
                onClick={() =>
                    navigate("/gallery")
                }
            >
                ← 공개 갤러리
            </button>

            <div className="image-detail-layout">

                <div className="image-detail-preview">
                    <img
                        src={`http://localhost:8080/api/image/public/${image.id}/view`}
                        alt={image.prompt}
                    />
                </div>

                <section className="image-detail-info">

                    <h1>공개 이미지</h1>

                    <h2>프롬프트</h2>
                    <p>{image.prompt}</p>

                    {image.negativePrompt && (
                        <>
                            <h2>제외할 요소</h2>
                            <p>
                                {image.negativePrompt}
                            </p>
                        </>
                    )}

                    {image.width > 0 &&
                        image.height > 0 && (
                            <>
                                <h2>크기</h2>

                                <p>
                                    {image.width}
                                    {" × "}
                                    {image.height}
                                </p>
                            </>
                        )}

                    <h2>생성일</h2>

                    <p>
                        {new Date(
                            image.createdAt
                        ).toLocaleString("ko-KR")}
                    </p>

                    <button
                        type="button"
                        className={`like-button ${
                            image.liked ? "active" : ""
                        }`}
                        onClick={handleLike}
                    >
                        {image.liked ? "❤️ 좋아요" : "🤍 좋아요"}
                        {" "}
                        {image.likeCount}
                    </button>

                    <button
                        type="button"
                        className="download-button"
                        onClick={() =>
                            window.open(
                                `http://localhost:8080/api/image/public/${image.id}/download`,
                                "_blank"
                            )
                        }
                    >
                        이미지 다운로드
                    </button>

                </section>
            </div>
        </main>
    );
}

export default PublicImageDetail;