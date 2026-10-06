import { useEffect, useState } from "react";
import {
    Navigate,
    useNavigate,
    useParams,
} from "react-router-dom";

import {
    apiFetch,
    readJsonResponse,
} from "../api/api";

import { useAuth } from "../context/AuthContext";

function ImageDetail() {
    const { imageId } = useParams();
    const navigate = useNavigate();

    const {
        isLoggedIn,
        authLoading,
    } = useAuth();

    const [image, setImage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!isLoggedIn || !imageId) {
            setLoading(false);
            return;
        }

        const loadImage = async () => {
            try {
                setLoading(true);
                setError("");

                const response = await apiFetch(
                    `/image/${imageId}`
                );

                const data =
                    await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(
                        data?.error ??
                        `이미지를 불러오지 못했습니다. (${response.status})`
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
    }, [imageId, isLoggedIn]);

    const handleFavorite = async () => {
        try {
            const response = await apiFetch(
                `/image/${imageId}/favorite`,
                {
                    method: "PATCH",
                }
            );

            const data =
                await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.error ??
                    "즐겨찾기 변경에 실패했습니다."
                );
            }

            setImage((previous) => ({
                ...previous,
                favorite: data.favorite,
            }));

        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleVisibility = async () => {
        try {
            const response = await apiFetch(
                `/image/${imageId}/visibility`,
                {
                    method: "PATCH",
                }
            );

            const data = await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.error ??
                    "공개 설정 변경에 실패했습니다."
                );
            }

            setImage((previous) => ({
                ...previous,
                publicImage: data.publicImage,
            }));

        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleDownload = async () => {
        try {
            const response = await apiFetch(
                `/image/${imageId}/download`
            );

            if (!response.ok) {
                throw new Error(
                    `다운로드에 실패했습니다. (${response.status})`
                );
            }

            const blob = await response.blob();
            const url = URL.createObjectURL(blob);

            const link =
                document.createElement("a");

            link.href = url;
            link.download =
                `ai-image-${imageId}.png`;

            document.body.appendChild(link);
            link.click();
            link.remove();

            URL.revokeObjectURL(url);

        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleDelete = async () => {
        const confirmed = window.confirm(
            "이 이미지를 삭제하시겠습니까?"
        );

        if (!confirmed) {
            return;
        }

        try {
            const response = await apiFetch(
                `/image/${imageId}`,
                {
                    method: "DELETE",
                }
            );

            const data =
                await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.error ??
                    "이미지 삭제에 실패했습니다."
                );
            }

            navigate("/my-images", {
                replace: true,
            });

        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleReuse = () => {
        navigate("/", {
            state: {
                prompt: image.prompt,
                negativePrompt:
                    image.negativePrompt ?? "",
                width:
                    image.width > 0
                        ? image.width
                        : 1024,
                height:
                    image.height > 0
                        ? image.height
                        : 1024,
            },
        });
    };

    if (authLoading) {
        return (
            <p className="page-message">
                로그인 확인 중...
            </p>
        );
    }

    if (!isLoggedIn) {
        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    if (loading) {
        return (
            <p className="page-message">
                이미지를 불러오는 중...
            </p>
        );
    }

    if (error) {
        return (
            <main className="image-detail-page">
                <p className="error-message">
                    {error}
                </p>

                <button
                    type="button"
                    onClick={() =>
                        navigate("/my-images")
                    }
                >
                    목록으로
                </button>
            </main>
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
                    navigate("/my-images")
                }
            >
                ← 내 이미지
            </button>

            <div className="image-detail-layout">
                <div className="image-detail-preview">
                    <img
                        src={image.imageUrl}
                        alt={image.prompt}
                    />
                </div>

                <section className="image-detail-info">
                    <h1>이미지 상세</h1>

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
                                <h2>이미지 크기</h2>

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

                    <div className="image-detail-actions">
                        <button
                            type="button"
                            onClick={handleFavorite}
                        >
                            {image.favorite
                                ? "★ 즐겨찾기 해제"
                                : "☆ 즐겨찾기"}
                        </button>

                        <button
                            type="button"
                            onClick={handleVisibility}
                        >
                            {image.publicImage
                                ? "🌐 공개 중"
                                : "🔒 비공개"}
                        </button>

                        <button
                            type="button"
                            onClick={handleReuse}
                        >
                            다시 사용
                        </button>

                        <button
                            type="button"
                            onClick={handleDownload}
                        >
                            다운로드
                        </button>

                        <button
                            type="button"
                            className="danger-button"
                            onClick={handleDelete}
                        >
                            삭제
                        </button>
                    </div>
                </section>
            </div>
        </main>
    );
}

export default ImageDetail;