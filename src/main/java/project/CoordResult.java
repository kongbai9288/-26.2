package project;

/**
 * 搜索结果：一个女巫小屋的坐标 + 高度信息。
 * 由 SearchCoords 在回调中产出，供 GUI / CLI 消费。
 */
public record CoordResult(int x, int y, int z, double density) {
    // record 自动生成 x()、y()、z()、density() 访问器
}
