package com.example.e_calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    
    // initializing all the required views, buttons and strings;
    TextView result_id, exp_id;
    Button clear, open_bracket, close_bracket, allClear;
    Button add, subtract, multiply, divide, decimal, calculate, button;
    Boolean decimalEnabled;
    int openBrCount = 0;  // handles paranthesis entries
    String exp, key;
    String ops = "x+-/";
    String warning = "Invalid format used.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exp_id = findViewById(R.id.expression);
        result_id = findViewById(R.id.result_id);
        result_id.setVisibility(View.VISIBLE);
        
        // setting btn ids for the numbers using the setBtnId and for loop
        for (int i = 0; i <= 9; i++) {
            @SuppressLint("DiscouragedApi") int buttonId = getResources().getIdentifier("num_" + i, "id", getPackageName());
            setBtnId((Button) findViewById(buttonId), buttonId);
        }
        // setting btn ids for functional buttons
        setBtnId(clear, R.id.clear);
        setBtnId(allClear, R.id.allClear);
        setBtnId(add, R.id.add);
        setBtnId(subtract, R.id.subtract);
        setBtnId(multiply, R.id.multiply);
        setBtnId(divide, R.id.divide);
        setBtnId(decimal, R.id.decimal);
        setBtnId(open_bracket, R.id.open_bracket);
        setBtnId(close_bracket, R.id.close_bracket);
        setBtnId(calculate, R.id.calculate);
    }

    
    // setting onclick listener
    void setBtnId(Button button, int id) {
        button = findViewById(id);
        button.setOnClickListener(MainActivity.this);
    }

    @Override
    public void onClick(View view) {
        button = (Button) view; // initializing button to be clicked
        
        key = button.getText().toString(); // Get text of clicked button
        exp = exp_id.getText().toString(); // get text of expression view

//        call function to set decimalEnabled to true or false according to updated expressions.

        // if the button clicked was all clear
        if (key.equals("AC")) {
            exp_id.setText("");   // set expression view to nothing
            result_id.setText("0");    // set result view to 0
            openBrCount = 0;
            return;
        }

        // if calculate button clicked
        if (key.equals("=")) {
            if (openBrCount>0){
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                return;
            }
            if(result_id.getText().toString().equals("0")){
                return;
            }
            exp_id.setText(result_id.getText());
            return;
        }
        // if clear button clicked remove last character
        if (button==findViewById(R.id.clear)) {
            if (exp.length() > 0) {
                if (getLastChar(exp).equals("(")){
                    openBrCount--;
                } else if (getLastChar(exp).equals(")")) {
                    openBrCount++;
                }
                exp = exp.substring(0, exp.length()-1);
            }
            if (exp.length() == 0) {
                result_id.setText("0");
                exp = "";
            }
        }
        // if decmial is clicked (rules imposed)
        else if (key.equals(".")){
            setDecimalEnabled();  // check and set enablement of decimal
            String lchar ="";
            if(!exp.isEmpty()){
                lchar = getLastChar(exp);
            }
            if (decimalEnabled){
                if (exp.isEmpty() || "()".contains(lchar) || ops.contains(lchar)){
                    if (")".equals(lchar)){
                        exp+="x";
                    }
                    exp += "0.";
                }else{
                    exp+=".";
                }
                decimalEnabled = false;  // disable decimalEnable after adding decimal
            }
        }
        // If parenthesis clicked
        else if ("()".contains(key)){
            exp = handleParenthesis(key);
        }
        // if any operand or an operator is clicked.
        else if(CheckLastOp()) {
            exp += key;
        }

        // setting expression view to entered expression
        exp_id.setText(exp);
        
        // calculating expression and setting the result view
        String result = evaluate_string();
        if (!result.equals("Err")){
            result_id.setText(result);
        }
    }

//    parsing the expression string and calculating it.
    String evaluate_string() {
        // try and catch to handle error exceptions
        try {
            // initializing stack for operands and operators
            Stack<Double> operandStack = new Stack<>();
            Stack<Character> operatorStack = new Stack<>();
            int i = 0;
            
//            ----------Logic for evaluating regular expression----------
            
            // Looping through each character in the expression string
            while (i < exp.length()) {
                char c = exp.charAt(i);  // getting character and position i

                // checking if the character is a decimal point
                if (Character.isDigit(c) || (c == '.' )) {

                    // if true, build a number and push it to the operand stack
                    StringBuilder numBuilder = new StringBuilder();

                    // executes while loop until:
                    // condition1 -> the position is less than exp length
                    // condition2 -> the character is a digit or a decimal point
                    while (i < exp.length() && (Character.isDigit(exp.charAt(i)) || exp.charAt(i) == '.')) {
                        numBuilder.append(exp.charAt(i));
                        i++;
                    }
                    // parsing the num builder to number and pushing it to the operandStack
                    double num = Double.parseDouble(numBuilder.toString());
                    operandStack.push(num);
                }

                // if the character at the current position is '+' or '-'
                else if (c == '+' || c == '-') {
                    // checking position and knowing if its unary minus
                    if (i == 0 || exp.charAt(i - 1) == '(') {
                        // Unary minus (negation) - if it is start position or '-' comes just after '('
                        i++;
                        // using the same num building method above
                        StringBuilder numBuilder = new StringBuilder("-");
                        while (i < exp.length() && (Character.isDigit(exp.charAt(i)) || exp.charAt(i) == '.')) {
                            numBuilder.append(exp.charAt(i));
                            i++;
                        }
                        // parsing the string to double and pushing it to stack.
                        double num = Double.parseDouble(numBuilder.toString());
                        operandStack.push(num);
                    }

                    else {
                        // loops if operator stack is not empty and if operator has higher precedence
                        // than the operator at top of the stack.
                        while (!operatorStack.isEmpty() && hasPrecedence(c, operatorStack.peek())) {
                            // popping elements and performing operation and appending result to the stack.
                            applyOperator(operatorStack.pop(), operandStack);
                        }
                        operatorStack.push(c);  // push current operator to operator stack
                        i++;
                    }
                }

                // if the character at the current position is '*' or '/'
                else if (c == 'x' || c == '/') {

                    // looping if op stack is not empty and current operator has higher precedence.
                    while (!operatorStack.isEmpty() && hasPrecedence(c, operatorStack.peek())) {
                        // performing operation
                        applyOperator(operatorStack.pop(), operandStack);
                    }
                    operatorStack.push(c);  // push current operator to operator stack
                    i++;

                }

                // if character is open bracket
                else if (c == '(') {
                    operatorStack.push(c);
                    i++;
                }
                // if character is closed bracket
                else if (c == ')') {
                    // loop in operator stack is not empty and
                    // the top of operator stack is not an open bracket.
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                        applyOperator(operatorStack.pop(), operandStack);
                    }
                    operatorStack.pop(); // Pop the '('
                    i++;
                }
                // Ignore other characters (whitespace, etc.)
                else {
                    i++;
                }
            }

            // when you reach the final arithmetic calculation
            while (!operatorStack.isEmpty()) {
                applyOperator(operatorStack.pop(), operandStack);
            }

            double result = operandStack.pop();
            return new DecimalFormat("#.##########").format(result);

        } catch (Exception e) {
            return "Err";
        }
    }

    // method that checks the precedence of characters.
    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        return ((op1 == '+' || op1 == '-') || (op2 == '+' || op2== '-') &&
                (op2 == '/' || op2 == 'x')) || (op1 == 'x' && op2 == '/');
    }

    // method to perform operation and push result to operand stack
    private void applyOperator(char operator, Stack<Double> operands) {

        double b = operands.pop();
        double a = operands.pop();
        switch (operator) {
            case '+':
                operands.push(a+b);
                break;
            case '-':
                operands.push(a-b);
                break;
            case 'x':
                operands.push(a*b);
                break;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                operands.push(a/b);
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

//    Function to check if last character entered was an operator.
//    mainly decides if the user can enter an operator or not.
    private Boolean CheckLastOp(){
//        if expression is empty and the entered key is an operator returns false.
        if (exp.isEmpty()){
            if (ops.contains(key)){
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
//        if the last index and the key entered are operators return false
        if (ops.contains(getLastChar(exp)) && ops.contains(key)){
            exp = exp.substring(0, exp.length()-1)+key;
            return false;
        } else if (getLastChar(exp).equals("(") && "/x".contains(key)) {
            Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            return false;
        } else if(getLastChar(exp).equals(")") && "0123456789.".contains(key)){
            exp+="x";
            if (key.equals(".")){
                exp+="0.";
            }
            return true;
        }
        return true;
    }

//    Function to get the last character in the String Expression.
    public String getLastChar(String exp){
        int last_index = exp.length()-1;
        String last_char = exp.substring(last_index);
        return last_char;
    }

//    Function to set Decimal Enabled
    public void setDecimalEnabled(){
        if (exp.isEmpty() || (!exp.isEmpty() && !lastNumberHasDecimal(exp))
                || (button==findViewById(R.id.clear) && getLastChar(exp).equals("."))
                ||(getLastChar(exp).equals(")")) || ops.contains(getLastChar(exp))){
            decimalEnabled = true;
        }
        else{
            decimalEnabled = false;
        }
    }

//    Function to Check if the Last Number Has Decimal.
    public static boolean lastNumberHasDecimal(String expression) {
        // Remove any trailing spaces from the expression
        expression = expression.trim();

        // Find the last number in the expression
        String lastNumber = getLastNumber(expression);

        // Check if the last number contains a decimal
        return lastNumber.contains(".");
    }

//    Function to get the Last Number in the Expression String
    private static String getLastNumber(String expression) {
        // Split the expression by operators (+, -, X, /) to get individual numbers
        String[] numbers = expression.split("[\\+\\-x/]");

        // Iterate over the numbers array to find the last non-empty number
        String lastNumber = "";
        for (String number : numbers) {
            if (!number.isEmpty()) {
                lastNumber = number;
            }
        }
        return lastNumber;
    }

//    handle the input of paranthesis based on certain conditions
    private String handleParenthesis(String paranthesiskey){
        String last_char="";
        if(!exp.isEmpty()){
            last_char = getLastChar(exp);
        }
        if(paranthesiskey.equals("(")){

            if (ops.contains(last_char) || last_char.equals("(")){
                exp += key;
                openBrCount+=1;
            }
            else{
                exp += "x"+key;
                openBrCount+=1;
            }
        }
        else if (paranthesiskey.equals(")")) {
            if (exp.isEmpty() || ops.contains(getLastChar(exp)) ||
                    getLastChar(exp).equals("(") || openBrCount==0){
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
            }
            else{
                exp += key;
                openBrCount--;
            }
        }
        exp_id.setText(exp);
        return exp;
    }
}

