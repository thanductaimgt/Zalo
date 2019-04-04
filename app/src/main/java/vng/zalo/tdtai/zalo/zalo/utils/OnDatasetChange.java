package vng.zalo.tdtai.zalo.zalo.utils;

import java.util.List;

public interface OnDatasetChange {
    void updateChanges(List<? extends Model> modelList);
}