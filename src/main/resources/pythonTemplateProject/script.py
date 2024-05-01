import pandas as pd

version = {"major": 3, "minor": 12}
print(f"Running on Python {version["major"]}.{version["minor"]}")

# Create the DataFrame
dataset_url = 'https://raw.githubusercontent.com/mwaskom/seaborn-data/master/iris.csv'
df: pd.DataFrame = pd.read_csv(dataset_url)

records_frame = 100

print(f"""
First {records_frame} records:
{df.head(records_frame)}

Description:
{df.describe()}

Sepal length to width correlation:
{df['sepal_length'].corr(df['sepal_width'])}

Petal length to width correlation:
{df['petal_length'].corr(df['petal_width'])}

Petal and sepal length correlation:
{df['petal_length'].corr(df['sepal_length'])}
""")
