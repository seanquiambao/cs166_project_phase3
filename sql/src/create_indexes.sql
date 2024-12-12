DROP INDEX IF EXISTS user_login_index; 
DROP INDEX IF EXISTS user_role_index; 
DROP INDEX IF EXISTS item_type_index; 
DROP INDEX IF EXISTS item_price_index; 
DROP INDEX IF EXISTS item_name_index; 

CREATE INDEX user_login_index
ON Users
USING BTREE
(login);

CREATE INDEX user_role_index
ON Users
USING BTREE
(role);

CREATE INDEX item_type_index
ON Items 
USING BTREE
(typeOfItem);

CREATE INDEX item_name_index
ON Items 
USING BTREE
(itemName);

CREATE INDEX item_price_index
ON Items 
USING BTREE
(price);
