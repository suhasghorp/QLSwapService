from locust import HttpUser, between, task
import json


class UserBehavior(HttpUser):
    wait_time = between(1, 2)

    @task(1)
    def create_post(self):
        headers = {'Content-type': 'application/json', 'Accept': 'application/json'}
        payload = "{\"ID\": \"738641\", \"swapType\": \"PAYER\", \"nominal\": 595000.0, \"startDate\": \"03-06-2012\", \"maturityDate\": \"03-06-2026\", \"fixedLegRate\": 0.028037, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\", \"fullResults\":true}"
        headers = {
            'Content-Type': 'application/json'
        }
        self.client.post("/qlservices/price/vanillaswap", data=str(payload),headers=headers)