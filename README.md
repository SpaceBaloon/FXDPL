# FXDPL
Library that provides convinient access to data for FX controls.
BasicDAO is needed to cache data. In this class cached data represented by javafx.collections.ObservableList.
BasicDAO is intended for manipulation data, it holds information about how to retrive and save data through ObjectQuery.
DataSource ties FX controls and BasicDAO through DataSource.RowObject and DataSource.RowObjectProperties.
Add listener to these properties we can reflect data changes, cursor moving etc.
