import { useEffect, useState } from "react";
import { apiFetch, readJsonResponse } from "../api/api";
import { useNavigate } from "react-router-dom";

function PublicGallery() {
    const [images, setImages] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [sort, setSort] = useState("latest");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        const loadImages = async () => {
            try {
                const response = await apiFetch(
                    `/image/public?page=${currentPage}&size=12&sort=${sort}`
                );

                const data =
                    await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(
                        "공개 이미지를 불러오지 못했습니다."
                    );
                }

                setImages(data?.images ?? []);
                setTotalPages(data?.totalPages ?? 0);
            } catch (e) {
                setError(e.message);
            } finally {
                setLoading(false);
            }
        };

        loadImages();
    }, [currentPage, sort]);

    if (loading) {
        return <p>불러오는 중...</p>;
    }

    return (
        <main className="my-images-page">
            <h1>공개 갤러리</h1>

            {error && (
                <p className="error-message">
                    {error}
                </p>
            )}

            <div className="gallery-sort">
                <button
                    type="button"
                    className={
                        sort === "latest"
                            ? "active"
                            : ""
                    }
                    onClick={() => {
                        setSort("latest");
                        setCurrentPage(0);
                    }}
                >
                    최신순
                </button>

                <button
                    type="button"
                    className={
                        sort === "popular"
                            ? "active"
                            : ""
                    }
                    onClick={() => {
                        setSort("popular");
                        setCurrentPage(0);
                    }}
                >
                    인기순
                </button>
            </div>

            <section className="image-grid">
                {images.map((image) => (
                    <article
                        key={image.id}
                        className="image-card"
                    >
                        <img
                            src={`http://localhost:8080/api/image/public/${image.id}/view`}
                            alt={image.prompt}
                            onClick={() =>
                                navigate(`/gallery/${image.id}`)
                            }
                        />
                        <div className="gallery-card-info">

                            <p
                                className="gallery-prompt"
                                title={image.prompt}
                            >
                                {image.prompt.length > 40
                                    ? `${image.prompt.slice(0, 40)}...`
                                    : image.prompt}
                            </p>
                            <span className="gallery-like-count">
                                ❤️ {image.likeCount ?? 0}
                            </span>
                        </div>
                    </article>
                ))}
            </section>

            {totalPages > 1 && (
                <div className="pagination">
                    <button
                        type="button"
                        disabled={currentPage === 0}
                        onClick={() =>
                            setCurrentPage(
                                (page) => page - 1
                            )
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
                            setCurrentPage(
                                (page) => page + 1
                            )
                        }
                    >
                        다음
                    </button>
                </div>
            )}
        </main>
    );
}

export default PublicGallery;