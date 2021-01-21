package com.github.harbby.astarte;

import com.github.harbby.astarte.api.Partition;
import com.github.harbby.astarte.api.Stage;
import com.github.harbby.astarte.api.Task;

public class ShuffleMapTask<E>
        implements Task<MapTaskState>
{
    private final Stage stage;
    private final Partition partition;

    public ShuffleMapTask(
            Stage stage,
            Partition partition)
    {
        this.stage = stage;
        this.partition = partition;
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    @Override
    public int getTaskId()
    {
        return partition.getId();
    }

    @Override
    public MapTaskState runTask(TaskContext taskContext)
    {
        stage.compute(partition, taskContext);
        return null;
    }
}
