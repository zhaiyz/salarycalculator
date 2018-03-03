package com.zhaiyz.salarycalculator;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText preTaxSalaryEditText;

    private EditText socialInsuranceBaseEditText;

    private CheckBox customSocialInsuranceBaseCheckBox;

    private EditText housingProvidentBaseEditText;

    private CheckBox customHousingProvidentBaseCheckBox;

    private Spinner supplementHousingProvidentRateSpinner;

    private Button cal;

    private TableView<String[]> detailTableView;

    private TextView about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preTaxSalaryEditText = findViewById(R.id.pre_tax_salary);
        socialInsuranceBaseEditText = findViewById(R.id.social_insurance_base);
        customSocialInsuranceBaseCheckBox = findViewById(R.id.custom_social_insurance_base);
        housingProvidentBaseEditText = findViewById(R.id.housing_provident_base);
        customHousingProvidentBaseCheckBox = findViewById(R.id.custom_housing_provident_base);
        supplementHousingProvidentRateSpinner = findViewById(R.id.supplement_housing_provident_rate);
        cal = findViewById(R.id.cal);
        detailTableView = findViewById(R.id.salary_detail_table_view);
        detailTableView.setVisibility(View.INVISIBLE);
        about = findViewById(R.id.about);
        about.setMovementMethod(new ScrollingMovementMethod());

        preTaxSalaryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString().trim())
                        || new BigDecimal(s.toString()).compareTo(BigDecimal.valueOf(2300)) == -1) {
                    detailTableView.setVisibility(View.INVISIBLE);
                    about.setVisibility(View.VISIBLE);

                    socialInsuranceBaseEditText.setText(String.valueOf(3902));
                    housingProvidentBaseEditText.setText(String.valueOf(2185));
                } else {
                    about.setVisibility(View.INVISIBLE);
                    cal();
                }
            }
        });

        cal.setOnClickListener(this);

        customSocialInsuranceBaseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                socialInsuranceBaseEditText.setFocusable(isChecked);
                socialInsuranceBaseEditText.setFocusableInTouchMode(isChecked);
                socialInsuranceBaseEditText.requestFocus();

                String content = socialInsuranceBaseEditText.getText().toString();
                socialInsuranceBaseEditText.setSelection(content.length());
            }
        });

        customHousingProvidentBaseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                housingProvidentBaseEditText.setFocusable(isChecked);
                housingProvidentBaseEditText.setFocusableInTouchMode(isChecked);
                housingProvidentBaseEditText.requestFocus();

                String content = housingProvidentBaseEditText.getText().toString();
                housingProvidentBaseEditText.setSelection(content.length());
            }
        });

        socialInsuranceBaseEditText.setOnClickListener(this);
        housingProvidentBaseEditText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cal: {
                if (TextUtils.isEmpty(preTaxSalaryEditText.getText().toString().trim())) {
                    break;
                } else {
                    // 关闭软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    about.setVisibility(View.INVISIBLE);
                    cal();

                    break;
                }
            }
            case R.id.social_insurance_base: {
                if (!customSocialInsuranceBaseCheckBox.isChecked()) {
                    Toast toast = Toast.makeText(this, "选中自定义后才可输入", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
            }
            case R.id.housing_provident_base: {
                if (!customHousingProvidentBaseCheckBox.isChecked()) {
                    Toast toast = Toast.makeText(this, "选中自定义后才可输入", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
            }
            default:
                break;
        }
    }

    private void cal() {
        BigDecimal preTaxSalary = new BigDecimal(preTaxSalaryEditText.getText().toString());
        BigDecimal socialInsuranceBase = getSocialInsuranceBase();
        BigDecimal housingProvidentBase = getHousingProvidentBase();
        BigDecimal supplementHousingProvidentRate = getSupplementHousingProvidentRate();

        // 养老保险金
        BigDecimal pensionInsurance = socialInsuranceBase.multiply(new BigDecimal("0.08")).setScale(1, BigDecimal.ROUND_UP);

        // 医疗保险金
        BigDecimal medicareInsurance = socialInsuranceBase.multiply(new BigDecimal("0.02")).setScale(1, BigDecimal.ROUND_UP);

        // 失业保险金
        BigDecimal unemploymentInsurance = socialInsuranceBase.multiply(new BigDecimal("0.005")).setScale(1, BigDecimal.ROUND_UP);

        // 基本住房公积金
        BigDecimal housingProvident = housingProvidentBase.multiply(new BigDecimal("0.07")).setScale(0, BigDecimal.ROUND_HALF_UP);

        // 补充住房公积金
        BigDecimal supplementHousingProvident = getSupplementHousingProvident(preTaxSalary, supplementHousingProvidentRate);

        // 社保/公积金支出
        BigDecimal insuranceSum = pensionInsurance.add(medicareInsurance).add(unemploymentInsurance).add(housingProvident).add(supplementHousingProvident);

        // 需要交个税部分
        BigDecimal needPayTax = preTaxSalary.subtract(insuranceSum);

        // 个税
        BigDecimal tax = getTax(needPayTax);

        // 所有支出
        BigDecimal expenditure = insuranceSum.add(tax);

        // 税后月薪
        BigDecimal postTaxSalary = preTaxSalary.subtract(expenditure);

        String[][] dataToShow = {
                {"个人支出明细"},
                {"养老保险（8%）", pensionInsurance.toString()},
                {"医疗保险（2%）", medicareInsurance.toString()},
                {"失业保险（0.5%）", unemploymentInsurance.toString()},
                {"社保合计", pensionInsurance.add(medicareInsurance).add(unemploymentInsurance).toString()},
                {"基本公积金(7%)", housingProvident.toString()},
                {"补充公积金(" + supplementHousingProvidentRateSpinner.getSelectedItem().toString() + ")", supplementHousingProvident.toString()},
                {"公积金合计", housingProvident.add(supplementHousingProvident).toString()},
//                            {"缴费合计", insuranceSum.toString()},
                {"计算个税月薪", needPayTax.toString()},
                {"个人所得税", tax.toString()},
                {"支出总额", expenditure.toString()}
        };

        detailTableView.setDataAdapter(new SimpleTableDataAdapter(this, dataToShow));
        detailTableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, "税后月薪", postTaxSalary.toEngineeringString()));
        detailTableView.setVisibility(View.VISIBLE);
    }

    // 计算社保基数
    // 社保范围3902-19512
    private BigDecimal getSocialInsuranceBase() {
        BigDecimal socialInsuranceBase = new BigDecimal(socialInsuranceBaseEditText.getText().toString());
        BigDecimal preTaxSalary = new BigDecimal(preTaxSalaryEditText.getText().toString());
        if (customSocialInsuranceBaseCheckBox.isChecked()) {
            if (socialInsuranceBase.compareTo(BigDecimal.valueOf(3902)) == -1) {
                socialInsuranceBaseEditText.setText(String.valueOf(3902));
            } else if (socialInsuranceBase.compareTo(BigDecimal.valueOf(19512)) == 1) {
                socialInsuranceBaseEditText.setText(String.valueOf(19512));
            }
        } else {
            if (preTaxSalary.compareTo(BigDecimal.valueOf(3902)) == -1) {
                socialInsuranceBaseEditText.setText(String.valueOf(3902));
            } else if (preTaxSalary.compareTo(BigDecimal.valueOf(19512)) == 1) {
                socialInsuranceBaseEditText.setText(String.valueOf(19512));
            } else {
                socialInsuranceBaseEditText.setText(preTaxSalaryEditText.getText());
            }
        }
        return new BigDecimal(socialInsuranceBaseEditText.getText().toString());
    }

    // 计算公积金
    // 2185-19512
    private BigDecimal getHousingProvidentBase() {
        BigDecimal housingProvidentBase = new BigDecimal(housingProvidentBaseEditText.getText().toString());
        BigDecimal preTaxSalary = new BigDecimal(preTaxSalaryEditText.getText().toString());
        if (customHousingProvidentBaseCheckBox.isChecked()) {
            if (housingProvidentBase.compareTo(BigDecimal.valueOf(2185)) == -1) {
                housingProvidentBaseEditText.setText(String.valueOf(2185));
            } else if (housingProvidentBase.compareTo(BigDecimal.valueOf(19512)) == 1) {
                housingProvidentBaseEditText.setText(String.valueOf(19512));
            }
        } else {
            if (preTaxSalary.compareTo(BigDecimal.valueOf(2185)) == -1) {
                housingProvidentBaseEditText.setText(String.valueOf(2185));
            } else if (preTaxSalary.compareTo(BigDecimal.valueOf(19512)) == 1) {
                housingProvidentBaseEditText.setText(String.valueOf(19512));
            } else {
                housingProvidentBaseEditText.setText(preTaxSalaryEditText.getText());
            }
        }
        return new BigDecimal(housingProvidentBaseEditText.getText().toString());
    }

    private BigDecimal getSupplementHousingProvidentRate() {
        String strRate = supplementHousingProvidentRateSpinner.getSelectedItem().toString();
        BigDecimal rate;
        switch (strRate) {
            case "无": {
                rate = BigDecimal.ZERO;
                break;
            }
            case "1%": {
                rate = new BigDecimal("0.01");
                break;
            }
            case "2%": {
                rate = new BigDecimal("0.02");
                break;
            }
            case "3%": {
                rate = new BigDecimal("0.03");
                break;
            }
            case "4%": {
                rate = new BigDecimal("0.04");
                break;
            }
            case "5%": {
                rate = new BigDecimal("0.05");
                break;
            }
            default: {
                rate = BigDecimal.ZERO;
                break;
            }
        }
        return rate;
    }

    // 上限1952，这是个人和公司的总上限，个人上限应该是976
    private BigDecimal getSupplementHousingProvident(BigDecimal preTaxSalary,
                                                     BigDecimal supplementHousingProvidentRate) {
        BigDecimal result = preTaxSalary.multiply(supplementHousingProvidentRate).setScale(0, BigDecimal.ROUND_HALF_UP);
        return result.compareTo(BigDecimal.valueOf(976)) == 1 ? BigDecimal.valueOf(976) : result;
    }

    // 计算个税
    private BigDecimal getTax(BigDecimal needPayTax) {
        if (needPayTax.compareTo(BigDecimal.valueOf(3500)) < 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal calTax = needPayTax.subtract(BigDecimal.valueOf(3500));
        BigDecimal tax;
        if (calTax.compareTo(BigDecimal.valueOf(1500)) == -1) {
            tax = getTax(calTax, "0.03", 0);
        } else if (calTax.compareTo(BigDecimal.valueOf(4500)) == -1) {
            tax = getTax(calTax, "0.1", 105);
        } else if (calTax.compareTo(BigDecimal.valueOf(9000)) == -1) {
            tax = getTax(calTax, "0.2", 555);
        } else if (calTax.compareTo(BigDecimal.valueOf(35000)) == -1) {
            tax = getTax(calTax, "0.25", 1005);
        } else if (calTax.compareTo(BigDecimal.valueOf(55000)) == -1) {
            tax = getTax(calTax, "0.3", 2755);
        } else if (calTax.compareTo(BigDecimal.valueOf(80000)) == -1) {
            tax = getTax(calTax, "0.35", 5505);
        } else {
            tax = getTax(calTax, "0.45", 13505);
        }

        return tax;
    }

    private BigDecimal getTax(BigDecimal calTax, String fee, int deduction) {
        return calTax.multiply(new BigDecimal(fee))
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .subtract(BigDecimal.valueOf(deduction));
    }
}
