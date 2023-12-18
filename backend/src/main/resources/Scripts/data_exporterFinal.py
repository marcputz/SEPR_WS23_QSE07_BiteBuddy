import os
import pandas as pd
import json

allrecipes = pd.read_csv('data/scraped-07-05-21.csv')
recipe_details = pd.read_csv('data/01_Recipe_Details.csv')
ingredients = pd.read_csv('data/02_Ingredients.csv')
compound_ingredients = pd.read_csv('data/03_Compound_Ingredients.csv')
ingredients_aliases = pd.read_csv('data/04_Recipe-Ingredients_Aliases.csv')
scraped_recipes=pd.read_csv('data/scraped-07-05-21.csv')
allergenes=pd.read_csv('data/allergies.csv')

json_recipes = []
json_ingredients = []
json_recipe_ingredients = []
json_allergene = []
json_allergene_ingredient = []
counter = 0
counter_allergenes_ingredients = 0
counter_ingredients = 0
recipe_counter = 0



def explore_data(id):
    global counter
    global recipe_counter
    global json_ingredients
    details = recipe_details[recipe_details['Recipe ID'] == id].iloc[0]
    ingredients = ingredients_aliases[ingredients_aliases['Recipe ID'] == id]
    instructions = allrecipes.loc[allrecipes["name"].str.lower() == details["Title"].lower(), "directions"].iloc[0]

    # saving recipe
    json_recipes.append({"id": recipe_counter + 1, "name": details["Title"], "instructions": instructions})
    # saving ingredients
    for index, ingredient in ingredients.iterrows():
        if int(ingredient["Entity ID"]) < 932:
            json_recipe_ingredients.append({"id": len(json_recipe_ingredients)+1,
                                "recipe":  recipe_counter + 1,
                                "ingredient": int(ingredient["Entity ID"] + 1),
                                "amount":  "100 g"})
    recipe_counter = recipe_counter + 1


def explore_ingredient_data(name):
    global counter_ingredients
    myIngredients = ingredients[ingredients["Aliased Ingredient Name"] == name]
    # saving ingredients
    for index, row in myIngredients.iterrows():
        json_ingredients.append({"id": counter_ingredients + 1,
                                 "name": name})
        counter_ingredients = counter_ingredients + 1


def explore_allergene_data(id):
    global counter_allergenes_ingredients
    myAllergenes = allergenes[allergenes['Allergene ID'] == id ].iloc[0]
    id = id + 1
    myIngredients = ingredients[ingredients['Category'].str.contains(myAllergenes['Title'])]
    if(id == 1):
        myIngredients = ingredients[ingredients['Category'].str.contains('Cereal')]
    if(id == 2):
        search_terms = ['prawn', 'crab', 'lobster']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[1], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[2], case=False))
        ]
    if(id == 3):
        search_terms = ['egg']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 4):
        search_terms = ['fish']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 5):
        search_terms = ['peanut']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 6):
        search_terms = ['soy']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 7):
        search_terms = ['Dairy']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False))]
    if(id == 8):
        search_terms = ['Nuts']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False))]
    if(id == 9):
        search_terms = ['Celery']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False))]
    if(id == 10):
        search_terms = ['Mustard']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 11):
        search_terms = ['Sesame']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 12):
        search_terms = ['juices','fruit','cider','Beer','Grapes','Tea','vinegar','Alcohol','Guacamole']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False)) |
            (ingredients['Category'].str.contains(search_terms[1], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[1], case=False)) |
            (ingredients['Category'].str.contains(search_terms[2], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[2], case=False)) |
            (ingredients['Category'].str.contains(search_terms[3], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[3], case=False)) |
            (ingredients['Category'].str.contains(search_terms[4], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[4], case=False)) |
            (ingredients['Category'].str.contains(search_terms[5], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[5], case=False)) |
            (ingredients['Category'].str.contains(search_terms[6], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[6], case=False)) |
            (ingredients['Category'].str.contains(search_terms[7], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[7], case=False)) |
            (ingredients['Category'].str.contains(search_terms[8], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[8], case=False))]
    if(id == 13):
        search_terms = ['lupin']
        myIngredients = ingredients[
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False))]
    if(id == 14):
        search_terms = ['mollusc', 'Clam','Oyster','Mussel','Scallop','Octopus','Squid','Snail','Abalone','Cuttlefish','Conch','Cockle']
        myIngredients = ingredients[
            (ingredients['Category'].str.contains(search_terms[0], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[0], case=False)) |
            (ingredients['Category'].str.contains(search_terms[1], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[1], case=False)) |
            (ingredients['Category'].str.contains(search_terms[2], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[2], case=False)) |
            (ingredients['Category'].str.contains(search_terms[3], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[3], case=False)) |
            (ingredients['Category'].str.contains(search_terms[4], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[4], case=False)) |
            (ingredients['Category'].str.contains(search_terms[5], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[5], case=False)) |
            (ingredients['Category'].str.contains(search_terms[6], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[6], case=False)) |
            (ingredients['Category'].str.contains(search_terms[7], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[7], case=False)) |
            (ingredients['Category'].str.contains(search_terms[8], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[8], case=False)) |
            (ingredients['Category'].str.contains(search_terms[9], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[9], case=False)) |
            (ingredients['Category'].str.contains(search_terms[10], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[10], case=False)) |
            (ingredients['Category'].str.contains(search_terms[11], case=False)) |
            (ingredients['Aliased Ingredient Name'].str.contains(search_terms[11], case=False))]


    # saving recipe
    json_allergene.append({"id": int(myAllergenes["Allergene ID"] + 1), "name": myAllergenes["Title"]})
    # saving ingredients
    for index, ingredient in myIngredients.iterrows():
        if int(ingredient["Entity ID"]) < 932:
            json_allergene_ingredient.append({"id": len(json_allergene_ingredient) + 1,
                                    "allergene": int(myAllergenes["Allergene ID"] +1),
                                    "ingredient":int(ingredient["Entity ID"] + 1)})



def find_matches():
    # sanity check, never actually used in creating the jsons
    found = 0
    allrecipes_titles = allrecipes["name"].str.lower().tolist()

    for index, row in recipe_details.iterrows():
        if (row['Title'].lower() in allrecipes_titles):
            found += 1
            print(row['Title'])

    print(f"found {found} matches")


if __name__ == '__main__':
    # # testing for titles from pictures
    # files = os.listdir("images")
    # titles = []
    #
    # for file in files:
    #     if file.endswith(".png"):
    #         titles.append(file.replace(".png", ""))
    # find_matches()
    # exit()

    titles = allrecipes["name"].str.lower().tolist()[:800]
    allergene_titles = allergenes["Title"].str.lower().tolist()
    ingredient_titles = ingredients["Aliased Ingredient Name"].str.lower().tolist()

    from tqdm import tqdm

    for allergene_title in tqdm(allergene_titles):
        allergene_id = allergenes.loc[allergenes["Title"].str.lower() == allergene_title, "Allergene ID"].tolist()

        if len(allergene_id) > 0:
            explore_allergene_data(allergene_id[0])

    for ingredient_title in tqdm(ingredient_titles):
        ingredient_name = ingredients.loc[ingredients["Aliased Ingredient Name"].str.lower() == ingredient_title, "Aliased Ingredient Name"].tolist()
        if len(ingredient_name) > 0:
            explore_ingredient_data(ingredient_name[0])

    breakcount  = 0
    for title in tqdm(titles):
        recipe_id = recipe_details.loc[recipe_details["Title"].str.lower() == title, "Recipe ID"].tolist()
        if recipe_counter > 800:
            break
        if len(recipe_id):
            explore_data(recipe_id[0])


     #saving results
    with open("generated/recipes.json", "w+") as f:
        json.dump(json_recipes, f, indent=4)

    with open("generated/RecipeIngredients.json", "w+") as f:
        json.dump(json_recipe_ingredients, f, indent=4)

    with open("generated/Ingredients.json", "w+") as f:
        json.dump(json_ingredients, f, indent=4)

    with open("generated/Allergene.json", "w+") as f:
        json.dump(json_allergene, f, indent=4)

    with open("generated/AllergeneIngredient.json", "w+") as f:
        json.dump(json_allergene_ingredient, f, indent=4)