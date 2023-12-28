package me.nullaqua.api.math;

import java.util.*;

public final class Calculator
{
    private final Map<String,Variable> variables=new HashMap<>();

    public Calculator()
    {
    }

    private static String toDBC(String input)
    {
        char[] c=input.toCharArray();
        for (int i=0;i<c.length;i++)
        {
            if (c[i]=='\u3000')
            {
                c[i]=' ';
            }
            else if (c[i]>'\uFF00'&&c[i]<'\uFF5F')
            {
                c[i]=(char) (c[i]-65248);
            }
        }
        return new String(c);
    }

    private static boolean isNum(char s)
    {
        return (s>='0'&&s<='9')||s=='.';
    }

    private static boolean isOperator(char s)
    {
        return "+-*/()".contains(s+"");
    }

    public void putItem(Variable variable)
    {
        variables.put(variable.getName(),variable);
    }

    public Set<Variable> getItems()
    {
        return new HashSet<>(variables.values());
    }

    public Variable removeItem(Variable variable)
    {
        return variables.remove(variable.getName());
    }

    public Result calculator(String str)
    {
        str=str.replace('（','(').replace('）','(').replace('×','*').replace('÷','/');
        str=toDBC(str);
        StringBuilder s;
        Stack<Character> a=new Stack<>();
        Stack<String> stack=new Stack<>();

        Map<Number,Long> items=new HashMap<>();


        for (int i=0;i<str.length();i++)
        {
            if (!isOperator(str.charAt(i)))
            {
                s=new StringBuilder();
                while (i<str.length()&&!isOperator(str.charAt(i)))
                {
                    s.append(str.charAt(i));
                    i++;
                }
                i--;
                try
                {
                    Long l=items.get(Double.parseDouble(s.toString()));
                    if (l==null)
                    {
                        l=0L;
                    }
                    items.put(Double.parseDouble(s.toString()),++l);
                    stack.push(s.toString());
                }
                catch (Exception e)
                {
                    Variable variable=variables.get(s.toString());
                    if (variable==null)
                    {
                        return new Result(new HashMap<>(),true,false,null);
                    }
                    Long l=items.get(variable);
                    if (l==null)
                    {
                        l=0L;
                    }
                    items.put(variable,++l);
                    stack.push(String.valueOf(variable.vault()));
                }
            }
            else if ("(".indexOf(str.charAt(i))>=0)
            {
                a.push(str.charAt(i));
            }
            else if (")".indexOf(str.charAt(i))>=0)
            {
                while (a.peek()!='(')
                {
                    stack.push(a.pop()+"");
                }
                a.pop();
            }
            else if ("*/".indexOf(str.charAt(i))>=0)
            {
                if (a.isEmpty())
                {
                    a.push(str.charAt(i));
                }
                else if ("*/".indexOf(a.peek())>=0)
                {
                    stack.push(a.pop()+"");
                    a.push(str.charAt(i));
                }
                else if ("(".indexOf(a.peek())==0)
                {
                    a.push(str.charAt(i));
                }
                else if ("+-".indexOf(a.peek())>=0)
                {
                    a.push(str.charAt(i));
                }
            }
            else if ("+-".indexOf(str.charAt(i))>=0)
            {
                if (a.isEmpty())
                {
                    a.push(str.charAt(i));
                }
                else if ("*/".indexOf(a.peek())>=0)
                {
                    stack.push(a.pop()+"");
                    a.push(str.charAt(i));
                }
                else if ("(".indexOf(a.peek())>=0)
                {
                    a.push(str.charAt(i));
                }
                else if ("+-".indexOf(a.peek())>=0)
                {
                    stack.push(a.pop()+"");
                    a.push(str.charAt(i));
                }
            }
        }
        while (!a.isEmpty())
        {
            stack.push(a.pop()+"");
        }
        Stack<String> ans=new Stack<>();
        while (!stack.isEmpty())
        {
            ans.push(stack.pop());
        }
        Result result=work(ans);
        return new Result(items,result.formatErr,result.mathErr,result.ans);
    }

    public Result work(Stack<String> str)
    {
        if (str==null)
        {
            return new Result(new HashMap<>(),false,false,null);
        }
        Stack<String> result=new Stack<>();
        while (!str.isEmpty())
        {
            if (!"+-*/".contains(str.peek()))
            {
                result.push(str.peek());
            }
            if ("+-*/".contains(str.peek()))
            {
                double x, y;
                if (!result.isEmpty())
                {
                    x=Double.parseDouble(result.pop());
                }
                else
                {
                    return new Result(new HashMap<>(),true,false,null);
                }
                if (!result.isEmpty())
                {
                    y=Double.parseDouble(result.pop());
                }
                else
                {
                    return new Result(new HashMap<>(),true,false,null);
                }
                if ("-".contains(str.peek()))
                {
                    double n=y-x;
                    result.push(String.valueOf(n));
                }
                if ("+".contains(str.peek()))
                {
                    double n=y+x;
                    result.push(String.valueOf(n));
                }
                if ("*".contains(str.peek()))
                {
                    double n=y*x;
                    result.push(String.valueOf(n));
                }
                if ("/".contains(str.peek()))
                {
                    if (x==0.0D)
                    {
                        return new Result(new HashMap<>(),false,true,null);
                    }
                    double n=y/x;
                    result.push(String.valueOf(n));
                }
            }
            str.pop();
        }
        return new Result(new HashMap<>(),false,false,Double.parseDouble(result.pop()));
    }

    public static class Result
    {
        private final Map<Number,Long> item;
        private final List<Number> itemList;
        private final boolean formatErr;
        private final boolean mathErr;
        private final Double ans;

        private Result(Map<Number,Long> item,boolean formatErr,boolean mathErr,Double ans)
        {
            this.item=item;
            this.formatErr=formatErr;
            this.mathErr=mathErr;
            this.ans=ans;
            itemList=new ArrayList<>();
            for (Map.Entry<Number,Long> entry: item.entrySet())
            {
                for (int i=0;i<entry.getValue();i++)
                {
                    itemList.add(entry.getKey());
                }
            }
        }

        public boolean hasError()
        {
            return formatErr||mathErr;
        }

        public boolean hasFormatErr()
        {
            return formatErr;
        }

        public boolean hasMathErr()
        {
            return mathErr;
        }

        public Double getAnswer()
        {
            return ans;
        }

        public Map<Number,Long> getItem()
        {
            return new HashMap<>(item);
        }

        public List<Number> getItemInList()
        {
            return new ArrayList<>(itemList);
        }

        @Override
        public String toString()
        {
            if (hasError())
            {
                return "Calculator.Result{ HasError: "+(hasFormatErr()?"FormatErr":"MathErr")+" }";
            }
            return "Calculator.Result{ ans: "+getAnswer()+",items: "+getItemInList().toString()+" }";
        }
    }
}