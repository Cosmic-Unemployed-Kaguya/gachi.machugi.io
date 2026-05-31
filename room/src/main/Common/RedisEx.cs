using StackExchange.Redis;

namespace Common;

public static class RedisEx
{
    public static string GetValue(this HashEntry[] entryArr, string key, string defaultValue)
    {
        var entry = entryArr.FirstOrDefault(e => e.Name == key);
        return entry.Value.IsNull ? defaultValue : entry.Value.ToString();
    }
}