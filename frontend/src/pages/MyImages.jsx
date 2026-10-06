import { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { apiFetch, readJsonResponse } from "../api/api";
import { useAuth } from "../context/AuthContext";

function MyImages() {
    const navigate = useNavigate();
    const { isLoggedIn, authLoading } = useAuth();

    const [images, setImages] = useState([]);

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const pageSize = 12;

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");


    const [searchInput, setSearchInput] = useState("");
    const [searchKeyword, setSearchKeyword] = useState("");

    const [favoriteOnly, setFavoriteOnly] = useState(false);

    const getErrorMessage = (data, status) => {
        return (
            data?.error ??
            data?.message ??
            `요청에 실패했습니다. (${status})`
        );
    };

    useEffect(() => {
        if (!isLoggedIn) {
            setLoading(false);
            return;
        }

        const loadImages = async () => {
            try {
                setLoading(true);
                setError("");

                const queryParams = new URLSearchParams({
                    page: String(currentPage),
                    size: String(pageSize),
                    favorite: String(favoriteOnly),
                });

                if (searchKeyword) {
                    queryParams.set("q", searchKeyword);
                }

                const response = await apiFetch(
                    `/image/my?${queryParams.toString()}`
                );

                const data = await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(
                        getErrorMessage(
                            data,
                            response.status
                        )
                    );
                }

                setImages(data?.images ?? []);
                setTotalPages(data?.totalPages ?? 0);
                setTotalElements(data?.totalElements ?? 0);
            } catch (requestError) {
                setError(requestError.message);
            } finally {
                setLoading(false);
            }
        };

        loadImages();
    }, [isLoggedIn, searchKeyword, currentPage, favoriteOnly]);

    const handleDelete = async (imageId) => {
        const confirmed = window.confirm(
            "이 이미지를 삭제하시겠습니까?"
        );

        if (!confirmed) {
            return;
        }

        try {
            setError("");

            const response = await apiFetch(
                `/image/${imageId}`,
                {
                    method: "DELETE",
                }
            );

            const data = await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(
                        data,
                        response.status
                    )
                );
            }

            setImages((previousImages) => {
                if (favoriteOnly && !data.favorite) {
                    return previousImages.filter(
                        (image) => image.id !== imageId
                    );
                }

                return previousImages.map((image) =>
                    image.id === imageId
                        ? {
                            ...image,
                            favorite: data.favorite,
                        }
                        : image
                );
            });

            setTotalElements((previousTotal) =>
                Math.max(previousTotal - 1, 0)
            );
        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleDownload = async (image) => {
        try {
            setError("");

            const response = await apiFetch(
                `/image/${image.id}/download`
            );

            if (!response.ok) {
                throw new Error(
                    `이미지 다운로드에 실패했습니다. (${response.status})`
                );
            }

            const blob = await response.blob();
            const objectUrl = URL.createObjectURL(blob);

            const link = document.createElement("a");

            link.href = objectUrl;
            link.download = `ai-image-${image.id}.png`;

            document.body.appendChild(link);
            link.click();
            link.remove();

            URL.revokeObjectURL(objectUrl);
        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleFavorite = async (imageId) => {
        try {
            setError("");

            const response = await apiFetch(
                `/image/${imageId}/favorite`,
                {
                    method: "PATCH",
                }
            );

            const data = await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.error ??
                    `즐겨찾기 변경에 실패했습니다. (${response.status})`
                );
            }

            setImages((previousImages) =>
                previousImages.map((image) =>
                    image.id === imageId
                        ? {
                            ...image,
                            favorite: data.favorite,
                        }
                        : image
                )
            );
        } catch (requestError) {
            setError(requestError.message);
        }
    };

    const handleReusePrompt = (image) => {
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


    const handleSearch = (event) => {
        event.preventDefault();

        setCurrentPage(0);
        setSearchKeyword(searchInput.trim());
    };

    const handleResetSearch = () => {
        setSearchInput("");
        setSearchKeyword("");
        setCurrentPage(0);
    };

    if (authLoading) {
        return <p className="page-message">로그인 확인 중...</p>;
    }

    if (!isLoggedIn) {
        return <Navigate to="/login" replace />;
    }

    if (loading) {
        return <p className="page-message">이미지 목록 불러오는 중...</p>;
    }

    return (
        <main className="my-images-page">
            <h1>내 이미지</h1>
            <p className="image-count">
                총 {totalElements}개의 이미지
            </p>

            <button
                type="button"
                className={`favorite-filter-button ${
                    favoriteOnly ? "active" : ""
                }`}
                onClick={() => {
                    setFavoriteOnly((previous) => !previous);
                    setCurrentPage(0);
                }}
            >
                {favoriteOnly
                    ? "★ 즐겨찾기만 보는 중"
                    : "☆ 즐겨찾기만 보기"}
            </button>
            <form
                className="image-search-form"
                onSubmit={handleSearch}
            >

                <input
                    type="search"
                    value={searchInput}
                    onChange={(event) =>
                        setSearchInput(event.target.value)
                    }
                    placeholder="프롬프트 검색"
                />


                <button type="submit">
                    검색
                </button>


                {searchKeyword && (
                    <button
                        type="button"
                        onClick={handleResetSearch}
                    >
                        초기화
                    </button>
                )}
            </form>

            {error && (
                <p className="error-message">{error}</p>
            )}

            {!error && images.length === 0 && (
                <p>
                    {searchKeyword
                        ? `"${searchKeyword}" 검색 결과가 없습니다.`
                        : "아직 생성한 이미지가 없습니다."}
                </p>
            )}

            <section className="image-grid">
                {images.map((image) => (
                    <article
                        key={image.id}
                        className="image-card"
                    >

                        <img
                            src={image.imageUrl}
                            alt={image.prompt}
                            className="image-card-preview"
                            onClick={() =>
                                navigate(`/images/${image.id}`)
                            }
                        />
                        <button
                            type="button"
                            className={`favorite-button ${
                                image.favorite ? "active" : ""
                            }`}
                            onClick={() =>
                                handleFavorite(image.id)
                            }
                        >
                            {image.favorite
                                ? "★ 즐겨찾기"
                                : "☆ 즐겨찾기"}
                        </button>

                        <div className="image-card-content">
                            <p
                                className="image-prompt image-detail-link"
                                onClick={() =>
                                    navigate(`/images/${image.id}`)
                                }
                            >
                                {image.prompt}
                            </p>

                            {image.negativePrompt && (
                                <p className="image-negative-prompt">
                                    제외: {image.negativePrompt}
                                </p>
                            )}

                            {image.width > 0 && image.height > 0 && (
                                <p className="image-size">
                                    {image.width} × {image.height}
                                </p>
                            )}

                            <time dateTime={image.createdAt}>
                                {new Date(
                                    image.createdAt
                                ).toLocaleString("ko-KR")}
                            </time>

                            <button
                                type="button"
                                className="reuse-prompt-button"
                                onClick={() => handleReusePrompt(image)}
                            >
                                다시 사용
                            </button>

                            <button
                                type="button"
                                className="download-image-button"
                                onClick={() => handleDownload(image)}
                            >
                                다운로드
                            </button>

                            <button
                                type="button"
                                className="delete-image-button"
                                onClick={() => handleDelete(image.id)}
                            >
                                삭제
                            </button>
                        </div>
                    </article>
                ))}
            </section>
            {totalPages > 1 && (
                <nav
                    className="pagination"
                    aria-label="내 이미지 페이지 이동"
                >
                    <button
                        type="button"
                        disabled={currentPage === 0}
                        onClick={() =>
                            setCurrentPage((page) => page - 1)
                        }
                    >
                        이전
                    </button>

                    <span>
            {currentPage + 1} / {totalPages}
        </span>

                    <button
                        type="button"
                        disabled={
                            currentPage >= totalPages - 1
                        }
                        onClick={() =>
                            setCurrentPage((page) => page + 1)
                        }
                    >
                        다음
                    </button>
                </nav>
            )}
        </main>
    );
}

export default MyImages;