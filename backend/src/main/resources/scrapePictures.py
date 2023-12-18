from selenium.webdriver.common.by import By
import time
from selenium import webdriver
import pandas as pd
from tqdm import tqdm
import os
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import json


def wait_for(xpath):
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, xpath)))


def search_for_image(title):
    url = "https://www.google.at/search?tbm=isch&q=" + title.replace(" ", "+")
    driver.get(url)


def save_first_image(titles, saving_path):
    time.sleep(1)
    image = driver.find_element(By.XPATH, '//*[@id="islrg"]/div[1]/div[1]/a[1]/div[1]/img')

    image.click()

    time.sleep(2)
    popup = driver.find_element(By.XPATH, '//*[@id="Sva75c"]')
    better_image = popup.find_elements(By.TAG_NAME, "img")[2]
    better_image.screenshot(f"{saving_path}/{titles}.png")


def scrape_for_images(df, saving_path="images"):
    files = os.listdir(saving_path)
    already_scraped = []

    for file in files:
        if file.endswith(".png"):
            already_scraped.append(file.replace(".png", ""))

    for index, row in tqdm(df.iterrows(), total=(len(titles)) - len(already_scraped)):
        if row['id'] not in already_scraped:
            search_for_image(row['name'])
            save_first_image(row['id'], saving_path=saving_path)


def generate_filtered_csv(titles):
    allrecipes = pd.read_csv("csv/allrecipes.csv", encoding="Windows-1252")

    # Recipes.json
    # ====================
    recipes = []
    for i, title in enumerate(titles):
        # getting instructions:
        instructions = allrecipes.loc[allrecipes["name"] == title, "summary"].iloc[0]

        recipes.append({"id": i, "name": title, "instructions": instructions})

    with open("FoodDataFiles/Recipes.json", "w+") as f:
        json.dump(recipes, f, indent=4)
    # ====================

    # Ingredients.json
    # ====================
    ingredients = []
    for title in titles:
        for ing in allrecipes.loc[allrecipes["name"] == title, "ingredients"]:
            # getting ingredients:
            ingredients.append({"id": i, "name": title, "ingredients": []})


if __name__ == '__main__':
    files = os.listdir("RecipePictures")
    titles = []

    for file in files:
        if file.endswith(".png"):
            titles.append(file.replace(".png", ""))

    # Webscraping
    driver = webdriver.Chrome()
    driver.implicitly_wait(5)

    df = pd.DataFrame()
    with open("FoodDataFiles/Recipes.json", "r") as f:
        data = json.load(f)
        df = pd.DataFrame().from_records(data)

    driver.get("https://www.google.at")

    print("waiting for you to confirm cookies")
    time.sleep(3)

    print("starting...")
    scrape_for_images(df, saving_path="RecipePictures")
