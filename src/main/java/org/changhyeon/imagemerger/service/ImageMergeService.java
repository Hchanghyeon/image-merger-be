package org.changhyeon.imagemerger.service;

import org.changhyeon.imagemerger.dto.ImageMergeRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageMergeService {

    public byte[] mergeImages(MultipartFile[] images, ImageMergeRequest request) throws IOException {

        // 1. 이미지 로드
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (MultipartFile file : images) {
            BufferedImage img = ImageIO.read(file.getInputStream());
            bufferedImages.add(img);
        }
        log.info("Image load complete size: {}", images.length);

        // 2. 리사이즈 처리 (resizeScale 적용)
        if (request.getResizeScale() != 100) {
            List<BufferedImage> resizedImages = new ArrayList<>();
            for (BufferedImage img : bufferedImages) {
                int newWidth = img.getWidth() * request.getResizeScale() / 100;
                int newHeight = img.getHeight() * request.getResizeScale() / 100;
                BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                resizedImages.add(resized);
            }
            bufferedImages = resizedImages;
            log.info("Image resize complete");
        }


        // 3. 명시적인 리사이즈 값이 있으면 적용 (resizeWidth, resizeHeight)
        if (request.getResizeWidth() != null && !request.getResizeWidth().isEmpty()
                && request.getResizeHeight() != null && !request.getResizeHeight().isEmpty()) {
            try {
                int newWidth = Integer.parseInt(request.getResizeWidth());
                int newHeight = Integer.parseInt(request.getResizeHeight());
                List<BufferedImage> resizedImages = new ArrayList<>();
                for (BufferedImage img : bufferedImages) {
                    BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = resized.createGraphics();
                    g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
                    g2d.dispose();
                    resizedImages.add(resized);
                }
                bufferedImages = resizedImages;
            } catch (NumberFormatException e) {
                // 파싱 실패 시 무시
            }
        }

        // 4. 최종 병합 이미지 크기 계산 및 병합 처리
        // 4. 최종 병합 이미지 크기 계산 및 병합 처리
        BufferedImage mergedImage;
        if (request.getOrientation().equalsIgnoreCase("Vertical")) {
            int maxWidth = bufferedImages.stream().mapToInt(BufferedImage::getWidth).max().orElse(0);
            int totalHeight = bufferedImages.stream().mapToInt(BufferedImage::getHeight).sum();
            int totalGap = request.getGaps().stream().mapToInt(Integer::intValue).sum() + request.getLastGap();
            totalHeight += totalGap;

            mergedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = mergedImage.createGraphics();
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, maxWidth, totalHeight);

            int y = 0;
            for (int i = 0; i < bufferedImages.size(); i++) {
                BufferedImage img = bufferedImages.get(i);
                int x = request.isCenterAlign() ? (maxWidth - img.getWidth()) / 2 : 0;
                g2d.drawImage(img, x, y, null);
                y += img.getHeight();
                if (i < request.getGaps().size()) {
                    y += request.getGaps().get(i);
                }
            }
            g2d.dispose();
        } else { // Horizontal
            int maxHeight = bufferedImages.stream().mapToInt(BufferedImage::getHeight).max().orElse(0);
            int totalWidth = bufferedImages.stream().mapToInt(BufferedImage::getWidth).sum();
            int totalGap = request.getGaps().stream().mapToInt(Integer::intValue).sum() + request.getLastGap();
            totalWidth += totalGap;

            mergedImage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = mergedImage.createGraphics();
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, totalWidth, maxHeight);

            int x = 0;
            for (int i = 0; i < bufferedImages.size(); i++) {
                BufferedImage img = bufferedImages.get(i);
                int y = request.isCenterAlign() ? (maxHeight - img.getHeight()) / 2 : 0;
                g2d.drawImage(img, x, y, null);
                x += img.getWidth();
                if (i < request.getGaps().size()) {
                    x += request.getGaps().get(i);
                }
            }
            g2d.dispose();
        }

        // 4.1. 자동 리사이징: 최대 이미지 크기가 65,500 픽셀을 초과하면 스케일 조정
        int maxDimension = 65500;
        if (mergedImage.getWidth() > maxDimension || mergedImage.getHeight() > maxDimension) {
            double scaleFactor = Math.min((double) maxDimension / mergedImage.getWidth(),
                    (double) maxDimension / mergedImage.getHeight());
            int newWidth = (int) (mergedImage.getWidth() * scaleFactor);
            int newHeight = (int) (mergedImage.getHeight() * scaleFactor);
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, mergedImage.getType());
            Graphics2D g2dScaled = scaledImage.createGraphics();
            g2dScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2dScaled.drawImage(mergedImage, 0, 0, newWidth, newHeight, null);
            g2dScaled.dispose();
            mergedImage = scaledImage;
        }

        // 5. 병합된 이미지를 지정 포맷으로 출력 (JPG인 경우, 투명도 제거)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (request.getFormat().equalsIgnoreCase("JPG") || request.getFormat().equalsIgnoreCase("JPEG")) {
            BufferedImage rgbImage = new BufferedImage(mergedImage.getWidth(), mergedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, mergedImage.getWidth(), mergedImage.getHeight());
            g2d.drawImage(mergedImage, 0, 0, null);
            g2d.dispose();
            ImageIO.write(rgbImage, "jpg", baos);
        } else {
            ImageIO.write(mergedImage, "png", baos);
        }
        log.info("complete all works");
        return baos.toByteArray();
    }
}
