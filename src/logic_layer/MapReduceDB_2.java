package logic_layer;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class MapReduceDB_2 {

  public static class TokenizerMapper extends Mapper<Object, Text, Text, FloatWritable>
  {

    private final static FloatWritable data = new FloatWritable();
    private Text word = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
    {
		int separatorIndex = value.find(","); //encuentra el indice de la coma en la linea leida
	
	    final String valueStr = value.toString();
	    if (separatorIndex < 0) 
	    {
	        System.err.printf("mapper: not enough records for %s", valueStr);
	        return;
	    }
	    String dateKey = valueStr.substring(0, separatorIndex).trim();
	    String token = valueStr.substring(separatorIndex + 1).trim().replaceAll("\\{Space}", "");
	    
	    SimpleDateFormat fmtFrom = new SimpleDateFormat("dd-MM-yyyy"); //formato de origen de la fecha
        SimpleDateFormat fmtTo = new SimpleDateFormat("MMMM-yyyy"); //formato de destino de la fecha, la que se va a almacenar
        
        try 
        {
            dateKey = fmtTo.format(fmtFrom.parse(dateKey));
            word.set(dateKey);
        } 
        catch (ParseException ex) 
        {
            System.err.printf("mapper: invalid key format %s", dateKey);
            return;
        }
        
        data.set(Float.parseFloat(token));
        context.write(word, data);
    }
  }

  public static class IntSumReducer extends Reducer<Text,FloatWritable,Text,IntWritable> 
  {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException 
    {
      int sum = 0;
      for (FloatWritable val : values) 
      {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }
}
