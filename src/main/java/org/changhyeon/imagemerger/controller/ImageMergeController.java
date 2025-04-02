package org.changhyeon.imagemerger.controller;

import org.changhyeon.imagemerger.dto.ImageMergeRequest;
import org.changhyeon.imagemerger.service.ImageMergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/image")
public class ImageMergeController {

    private final ImageMergeService imageMergeService;

    @Autowired
    public ImageMergeController(ImageMergeService imageMergeService) {
        this.imageMergeService = imageMergeService;
    }

    @PostMapping("/merge")
    public ResponseEntity<ByteArrayResource> mergeImages(
            @RequestPart("images") MultipartFile[] images,
            @RequestPart("settings") ImageMergeRequest request) throws Exception {

        byte[] mergedImageBytes = imageMergeService.mergeImages(images, request);

        String filename = "merged_result." +
                (request.getFormat().equalsIgnoreCase("JPG") || request.getFormat().equalsIgnoreCase("JPEG") ? "jpg" : "png");

        ByteArrayResource resource = new ByteArrayResource(mergedImageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(request.getFormat().equalsIgnoreCase("JPG") || request.getFormat().equalsIgnoreCase("JPEG") ?
                MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
