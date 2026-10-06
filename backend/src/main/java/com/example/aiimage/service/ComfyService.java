package com.example.aiimage.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ComfyService {

    private static final String COMFY_URL = "http://127.0.0.1:8188";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public ComfyService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(COMFY_URL)
                .build();
    }

    public ImageResult generateImage(
            String prompt,
            String negativePrompt,
            int width,
            int height
    ) {
        JsonNode workflow = readWorkflow();

        changePrompt(workflow, prompt);
        changeNegativePrompt(workflow, negativePrompt);
        changeImageSize(workflow, width, height);
        changeSeed(workflow);

        String promptId = sendPrompt(workflow);
        ImageInfo imageInfo = waitForImage(promptId);
        String imageUrl = createImageUrl(imageInfo);

        return new ImageResult(
                promptId,
                imageUrl,
                imageInfo.filename(),
                imageInfo.subfolder(),
                imageInfo.type()
        );
    }

    private JsonNode readWorkflow() {
        ClassPathResource resource =
                new ClassPathResource("workflow/flux_schnell.json");

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "workflow JSON 파일을 읽지 못했습니다.",
                    e
            );
        }
    }

    private void changePrompt(JsonNode workflow, String prompt) {
        JsonNode promptNode = workflow.path("6").path("inputs");

        if (!(promptNode instanceof ObjectNode promptInputs)) {
            throw new IllegalStateException(
                    "워크플로우에서 프롬프트 노드 6번을 찾지 못했습니다."
            );
        }

        promptInputs.put("text", prompt);
    }

    private void changeSeed(JsonNode workflow) {
        JsonNode samplerNode = workflow.path("31").path("inputs");

        if (!(samplerNode instanceof ObjectNode samplerInputs)) {
            throw new IllegalStateException(
                    "워크플로우에서 sampler 노드 31번을 찾지 못했습니다."
            );
        }

        long randomSeed = ThreadLocalRandom.current()
                .nextLong(0, 1_000_000_000_000_000L);

        samplerInputs.put("seed", randomSeed);
    }

    private String sendPrompt(JsonNode workflow) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.set("prompt", workflow);

        String responseBody = restClient.post()
                .uri("/prompt")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException(
                    "ComfyUI 생성 요청 응답이 비어 있습니다."
            );
        }

        JsonNode responseJson = objectMapper.readTree(responseBody);

        String promptId = responseJson
                .path("prompt_id")
                .asString();

        if (promptId.isBlank()) {
            throw new IllegalStateException(
                    "ComfyUI 응답에 prompt_id가 없습니다: " + responseBody
            );
        }

        return promptId;
    }

    private ImageInfo waitForImage(String promptId) {
        int maxAttempts = 120;
        long delayMillis = 1000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ImageInfo imageInfo = getImageFromHistory(promptId);

            if (imageInfo != null) {
                return imageInfo;
            }

            try {
                Thread.sleep(delayMillis);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "이미지 생성 대기 중 작업이 중단되었습니다.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "이미지 생성 시간이 초과되었습니다."
        );
    }

    private ImageInfo getImageFromHistory(String promptId) {
        String responseBody = restClient.get()
                .uri("/history/{promptId}", promptId)
                .retrieve()
                .body(String.class);

        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        JsonNode historyJson = objectMapper.readTree(responseBody);
        JsonNode promptHistory = historyJson.path(promptId);

        if (promptHistory.isMissingNode() || promptHistory.isEmpty()) {
            return null;
        }

        JsonNode outputs = promptHistory.path("outputs");

        if (!outputs.isObject()) {
            return null;
        }

        for (JsonNode outputNode : outputs) {
            JsonNode images = outputNode.path("images");

            if (!images.isArray() || images.isEmpty()) {
                continue;
            }

            JsonNode firstImage = images.get(0);

            String filename = firstImage
                    .path("filename")
                    .asString();

            String subfolder = firstImage
                    .path("subfolder")
                    .asString("");

            String type = firstImage
                    .path("type")
                    .asString("output");

            if (!filename.isBlank()) {
                return new ImageInfo(
                        filename,
                        subfolder,
                        type
                );
            }
        }

        return null;
    }

    private String createImageUrl(ImageInfo imageInfo) {
        String filename = encode(imageInfo.filename());
        String subfolder = encode(imageInfo.subfolder());
        String type = encode(imageInfo.type());

        return "http://localhost:8080/api/image/view"
                + "?filename=" + filename
                + "&subfolder=" + subfolder
                + "&type=" + type;
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    public record ImageResult(
            String promptId,
            String imageUrl,
            String filename,
            String subfolder,
            String type
    ) {
    }

    private record ImageInfo(
            String filename,
            String subfolder,
            String type
    ) {
    }

    public byte[] getImage(
            String filename,
            String subfolder,
            String type
    ) {
        byte[] imageBytes = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/view")
                        .queryParam("filename", filename)
                        .queryParam("subfolder", subfolder)
                        .queryParam("type", type)
                        .build()
                )
                .retrieve()
                .body(byte[].class);

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalStateException("이미지 데이터를 가져오지 못했습니다.");
        }

        return imageBytes;
    }

    private void changeNegativePrompt(
            JsonNode workflow,
            String negativePrompt
    ) {
        JsonNode negativeNode =
                workflow.path("33").path("inputs");

        if (!(negativeNode instanceof ObjectNode negativeInputs)) {
            throw new IllegalStateException(
                    "워크플로우에서 네거티브 프롬프트 노드 33번을 찾지 못했습니다."
            );
        }

        String value = negativePrompt == null
                ? ""
                : negativePrompt.trim();

        negativeInputs.put("text", value);
    }

    private void changeImageSize(
            JsonNode workflow,
            int width,
            int height
    ) {
        JsonNode latentNode =
                workflow.path("27").path("inputs");

        if (!(latentNode instanceof ObjectNode latentInputs)) {
            throw new IllegalStateException(
                    "워크플로우에서 이미지 크기 노드 27번을 찾지 못했습니다."
            );
        }

        latentInputs.put("width", width);
        latentInputs.put("height", height);
    }
}