#!/usr/bin/python3
import subprocess
import random
import time
import requests
import json

currency = ['RMB', 'USD', 'JPY', 'EUR']

url = 'http://202.120.40.8:30433'

user_id = random.randint(0,99999)
time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) 

initiator = "RMB"
if user_id < 40000:
    initiator = "RMB"
elif user_id < 70000:
    initiator = "USD"
elif user_id < 90000:
    initiator = "JPY"
else:
    initiator = "EUR"

commodity_number = random.randint(1,4)
items = []
for i in range(commodity_number):
    item_number = random.randint(1,10)
    item_id = random.randint(0,999)
    items.append({"id":item_id, "number":item_number})

order = {"user_id":user_id, "initiator":initiator, "time":time, "items":items}
order_json = json.dumps(order)

print(order_json)

#result = requests.post(url, json=order_json)  
#result.raise_for_status()