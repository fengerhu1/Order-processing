#!/usr/bin/python3
import random
filename = "/home/allen/Desktop/add_commodity.sql"
with open(filename, 'w') as file:
    for i in range(100):
        name = "商品"+str(i+1)
        price = str(round(random.uniform(10,99.9),1))
        currency = "RMB"
        rand_int = random.randint(1,100)
        if rand_int < 40:
            currency = "RMB"
        elif rand_int < 70:
            currency = "USD"
        elif rand_int < 90:
            currency = "JPY"
        else:
            currency = "EUR"
        inventory = str(random.randint(10000,99999))

        commodity = "\""+name+"\""+","+price+","+"\""+currency+"\""+","+inventory
        file.write("INSERT INTO commodity(Name, price, currency, inventory) VALUES("+commodity+");\n")
