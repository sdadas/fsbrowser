package pl.sdadas.fsbrowser.view.common.loading;

import java.util.function.Supplier;

/**
 * @author Sławomir Dadas
 */
public class Progress {

    private Supplier<String> supplier;

    public String getValue() {
        return supplier != null ? supplier.get() : null;
    }

    public Supplier<String> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<String> supplier) {
        this.supplier = supplier;
    }
}
