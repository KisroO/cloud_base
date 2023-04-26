import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Kisro
 * @since 2023/1/11
 **/
public class Test {
    private static List<NdSimResponse> list = new ArrayList<NdSimResponse>();

    static {
        NdSimResponse n1 = NdSimResponse.builder()
                .chassisNumber("NL400181")
                .vin("XXXXXXXXXNL400181")
                .productionCode("202312_02_01")
                .simNumber("10842324141")
                .seriesCode("K")
                .build();
        NdSimResponse n2 = NdSimResponse.builder()
                .chassisNumber("NL400181")
                .vin("XXXXXXXXXNL400181")
                .productionCode("202312_02_01")
                .simNumber("10842324111")
                .seriesCode("T")
                .build();
        NdSimResponse n3 = NdSimResponse.builder()
                .chassisNumber("NL400181")
                .vin("XXXXXXXXXNL400181")
                .productionCode("202312_02_01")
                .simNumber("10842324222")
                .seriesCode("Z")
                .build();
        list.add(n1);
        list.add(n2);
        list.add(n3);
    }

    public static void main(String[] args) {
        list.sort(Comparator.comparing(NdSimResponse::getChassisNumber)
                .reversed()
                .thenComparing(data -> !data.isNewRecorder()));
        System.out.println(list.get(0));
    }
}
