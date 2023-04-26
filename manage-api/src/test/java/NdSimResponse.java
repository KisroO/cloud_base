import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Kisro
 * @since 2023/1/12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NdSimResponse {
    private String vin;
    private String chassisNumber;
    private String simNumber;
    private String productionCode;
    /**
     * 终端代号
     */
    private String seriesCode;

    public boolean isNewRecorder() {
        if (StringUtils.isBlank(seriesCode)) {
            return false;
        }
        return "T".equals(seriesCode) || "R".equals(seriesCode);
    }
}
