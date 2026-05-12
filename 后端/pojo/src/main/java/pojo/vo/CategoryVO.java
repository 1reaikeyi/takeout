package pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
@Data
@Builder
public class CategoryVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 类型：1菜品分类 2套餐分类
     */
    private Long type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Long sort;
    /**
     * 状态：0停售 1起售
     */
    private Long status;

}
