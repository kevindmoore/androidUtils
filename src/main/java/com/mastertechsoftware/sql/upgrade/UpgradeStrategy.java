package com.mastertechsoftware.sql.upgrade;

import com.mastertechsoftware.sql.BaseDatabaseHelper;
/**
 * Add classes that implement this strategy to upgrade your database
 */
public interface UpgradeStrategy {
    void setVersions(int oldVersion, int newVersion);
    void loadData(BaseDatabaseHelper helper);
    void onDelete(BaseDatabaseHelper helper);
    void addData(BaseDatabaseHelper helper);

}
