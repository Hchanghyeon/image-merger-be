package org.changhyeon.imagemerger.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImageMergeRequest {
    private String orientation;
    private String format;
    private List<Integer> gaps;
    private int lastGap;
    private int resizeScale;
    private String resizeWidth;
    private String resizeHeight;
    private boolean centerAlign;
    private boolean autoResize;
}
