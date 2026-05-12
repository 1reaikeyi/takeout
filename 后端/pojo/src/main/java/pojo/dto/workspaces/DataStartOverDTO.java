package pojo.dto.workspaces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据概览查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStartOverDTO implements Serializable {

    /**
     * 开始时间
     */
    private LocalDateTime begin;

    /**
     * 结束时间
     */
    private LocalDateTime end;

}