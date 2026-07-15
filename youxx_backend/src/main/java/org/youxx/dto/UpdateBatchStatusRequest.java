package org.youxx.dto;

import lombok.Data;

import java.util.List;

/** 批量状态更新请求 */
@Data
public class UpdateBatchStatusRequest {
    private List<String> ids;
    private String status;
}
