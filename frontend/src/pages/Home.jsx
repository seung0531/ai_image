import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiFetch, readJsonResponse } from "../api/api";

function Home() {
    const location = useLocation();
    const navigate = useNavigate();
    const { email, isLoggedIn } = useAuth();

    const [elapsedSeconds, setElapsedSeconds] = useState(0);
    const [prompt, setPrompt] = useState("");
    const [imageUrl, setImageUrl] = useState("");
    const [imageId, setImageId] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [negativePrompt, setNegativePrompt] = useState("");
    const [width, setWidth] = useState(1024);
    const [height, setHeight] = useState(1024);

    useEffect(() => {
        if (!loading) {
            setElapsedSeconds(0);
            return;
        }

        const timerId = window.setInterval(() => {
            setElapsedSeconds((previousSeconds) =>
                previousSeconds + 1
            );
        }, 1000);

        return () => {
            window.clearInterval(timerId);
        };
    }, [loading]);

    useEffect(() => {
        const reusedPrompt =
            location.state?.prompt;

        if (!reusedPrompt) {
            return;
        }

        setPrompt(reusedPrompt);

        setNegativePrompt(
            location.state?.negativePrompt ?? ""
        );

        setWidth(
            location.state?.width ?? 1024
        );

        setHeight(
            location.state?.height ?? 1024
        );

        navigate("/", {
            replace: true,
            state: null,
        });
    }, [location.state, navigate]);

    const generateImage = async () => {
        if (loading) {
            return;
        }

        if (!prompt.trim()) {
            setError("프롬프트를 입력해 주세요.");
            return;
        }

        try {
            setLoading(true);
            setElapsedSeconds(0);
            setError("");
            setImageUrl("");
            setImageId("");

            const response = await apiFetch(
                "/image/generate",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        prompt: prompt.trim(),
                        negativePrompt:
                            negativePrompt.trim(),
                        width,
                        height,
                    }),
                }
            );

            const data =
                await readJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                    data?.message ??
                    data?.error ??
                    `이미지 생성에 실패했습니다. (${response.status})`
                );
            }

            if (!data?.imageUrl) {
                throw new Error(
                    "생성된 이미지 주소가 없습니다."
                );
            }

            setImageId(data.id);
            setImageUrl(data.imageUrl);
        } catch (requestError) {
            console.error(requestError);

            setError(
                requestError.message ||
                "이미지 생성 중 오류가 발생했습니다."
            );
        } finally {
            setLoading(false);
        }
    };

    if (!isLoggedIn) {
        return (
            <main className="image-page">
                <h1>AI 이미지 생성</h1>
                <p>로그인 후 이미지를 생성할 수 있습니다.</p>

                <Link to="/login">로그인하러 가기</Link>
            </main>
        );
    }

    return (
        <main className="image-page">
            <h1>AI 이미지 생성</h1>
            <p>{email}님, 로그인되었습니다.</p>

            <textarea
                value={prompt}
                onChange={(event) => setPrompt(event.target.value)}
                placeholder="생성할 이미지를 설명해 주세요."
                rows={5}
                disabled={loading}
            />

            <label htmlFor="negativePrompt">
                제외할 요소
            </label>

            <textarea
                id="negativePrompt"
                value={negativePrompt}
                onChange={(event) =>
                    setNegativePrompt(event.target.value)
                }
                placeholder="예: blurry, low quality, distorted"
                rows={3}
                disabled={loading}
            />

            <div className="image-size-options">
                <label>
                    이미지 비율

                    <select
                        value={`${width}x${height}`}
                        onChange={(event) => {
                            const [nextWidth, nextHeight] =
                                event.target.value
                                    .split("x")
                                    .map(Number);

                            setWidth(nextWidth);
                            setHeight(nextHeight);
                        }}
                        disabled={loading}
                    >
                        <option value="1024x1024">
                            정사각형 1:1
                        </option>

                        <option value="1024x768">
                            가로형 4:3
                        </option>

                        <option value="768x1024">
                            세로형 3:4
                        </option>

                        <option value="1344x768">
                            와이드 16:9
                        </option>

                        <option value="768x1344">
                            세로형 9:16
                        </option>
                    </select>
                </label>
            </div>

            <button
                type="button"
                onClick={generateImage}
                disabled={loading || !prompt.trim()}
            >
                {loading
                    ? `이미지 생성 중... ${elapsedSeconds}초`
                    : "이미지 생성"}
            </button>

            {loading && (
                <section
                    className="generation-loading"
                    aria-live="polite"
                >
                    <div className="loading-spinner" />

                    <div>
                        <strong>
                            AI가 이미지를 생성하고 있습니다.
                        </strong>

                        <p>
                            경과 시간: {elapsedSeconds}초
                        </p>

                        <p className="loading-description">
                            이미지 크기와 GPU 상태에 따라
                            시간이 조금 걸릴 수 있습니다.
                        </p>
                    </div>
                </section>
            )}

            {error && (
                <p className="error-message">{error}</p>
            )}

            {imageUrl && (
                <section className="generated-image">
                    <h2>생성 결과</h2>

                    <img
                        src={imageUrl}
                        alt={prompt}
                        onError={() => {
                            setError(
                                "생성된 이미지를 불러오지 못했습니다."
                            );
                        }}
                    />

                    <button
                        type="button"
                        onClick={() => {
                            window.open(
                                `http://localhost:8080/api/image/${imageId}/download`,
                                "_blank"
                            );
                        }}
                    >
                        이미지 다운로드
                    </button>
                </section>
            )}
        </main>
    );
}

export default Home;