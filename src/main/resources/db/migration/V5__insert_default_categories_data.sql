-- Migration: Insert default "system" categories
-- Version: V5
-- Description: Seeds hierarchical default categories owned by user_id = 'system'
--              Using Icons8 Color PNG icons (96px).
-- Attribution: Icons by Icons8 (https://icons8.com)

-- Ensure pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-----------------------------------------------------------------------
-- 0) Precondition: ensure the "system" user exists (FK to app_user)
-----------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_user WHERE username = 'system') THEN
        RAISE EXCEPTION 'Precondition failed: app_user(username=system) not found. Create it before running V5.';
    END IF;
END$$;

-----------------------------------------------------------------------
-- 1) Staging: declare the default categories with Icons8 PNG icons
--    Format: https://img.icons8.com/color/96/<icon-name>.png
-----------------------------------------------------------------------
CREATE TEMP TABLE _default_categories (
    name              text        NOT NULL,
    category_type     text        NOT NULL,
    color             text        NOT NULL,
    icon_url          text        NOT NULL,
    parent_name       text        NULL
) ON COMMIT DROP;

INSERT INTO _default_categories (name, category_type, color, icon_url, parent_name)
VALUES
    -- EXPENSE: Housing
    ('Housing',       'EXPENSE', '#5965F2', 'https://img.icons8.com/color/96/home.png', NULL),
    ('Rent',          'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/key.png', 'Housing'),
    ('Mortgage',      'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/bank.png', 'Housing'),
    ('Utilities',     'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/electricity.png', 'Housing'),
    ('Maintenance',   'EXPENSE', '#7B88FF', 'https://img.icons8.com/color/96/tools.png', 'Housing'),

    -- EXPENSE: Transportation
    ('Transportation','EXPENSE', '#2CB1BC', 'https://img.icons8.com/color/96/car.png', NULL),
    ('Public Transit','EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/bus.png', 'Transportation'),
    ('Fuel',          'EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/gas-station.png', 'Transportation'),
    ('Taxi & Ridehailing','EXPENSE','#55C2CB','https://img.icons8.com/color/96/taxi.png','Transportation'),
    ('Parking',       'EXPENSE', '#55C2CB', 'https://img.icons8.com/color/96/parking.png', 'Transportation'),

    -- EXPENSE: Food & Dining
    ('Food & Dining', 'EXPENSE', '#F26457', 'https://img.icons8.com/color/96/restaurant.png', NULL),
    ('Groceries',     'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/shopping-cart.png', 'Food & Dining'),
    ('Restaurants',   'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/cutlery.png', 'Food & Dining'),
    ('Coffee & Snacks','EXPENSE','#F58A80', 'https://img.icons8.com/color/96/coffee.png', 'Food & Dining'),
    ('Delivery',      'EXPENSE', '#F58A80', 'https://img.icons8.com/color/96/delivery.png', 'Food & Dining'),

    -- EXPENSE: Entertainment
    ('Entertainment', 'EXPENSE', '#A46BF5', 'https://img.icons8.com/color/96/party-balloons.png', NULL),
    ('Streaming',     'EXPENSE', '#BE90FF', 'https://img.icons8.com/color/96/tv.png', 'Entertainment'),
    ('Movies & Events','EXPENSE','#BE90FF', 'https://img.icons8.com/color/96/ticket.png', 'Entertainment'),
    ('Games',         'EXPENSE', '#BE90FF', 'https://img.icons8.com/color/96/controller.png', 'Entertainment'),

    -- EXPENSE: Health
    ('Health',        'EXPENSE', '#2ECC71', 'https://img.icons8.com/color/96/first-aid-kit.png', NULL),
    ('Pharmacy',      'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/pill.png', 'Health'),
    ('Doctor',        'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/doctor-male.png', 'Health'),
    ('Insurance',     'EXPENSE', '#71D79A', 'https://img.icons8.com/color/96/insurance-agent.png', 'Health'),

    -- EXPENSE: Shopping
    ('Shopping',      'EXPENSE', '#F2B84B', 'https://img.icons8.com/color/96/shopping-bag.png', NULL),
    ('Clothing',      'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/t-shirt.png', 'Shopping'),
    ('Electronics',   'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/laptop.png', 'Shopping'),
    ('Home Goods',    'EXPENSE', '#F7CD7F', 'https://img.icons8.com/color/96/sofa.png', 'Shopping'),

    -- EXPENSE: Travel
    ('Travel',        'EXPENSE', '#FF8C42', 'https://img.icons8.com/color/96/airplane-take-off.png', NULL),
    ('Flights',       'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/airplane.png', 'Travel'),
    ('Hotels',        'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/5-star-hotel.png', 'Travel'),
    ('Activities',    'EXPENSE', '#FFB27E', 'https://img.icons8.com/color/96/beach.png', 'Travel'),

    -- EXPENSE: Personal Care
    ('Personal Care', 'EXPENSE', '#E57AB3', 'https://img.icons8.com/color/96/spa.png', NULL),
    ('Salon',         'EXPENSE', '#F0A2CC', 'https://img.icons8.com/color/96/barber-pole.png', 'Personal Care'),
    ('Gym & Fitness', 'EXPENSE', '#F0A2CC', 'https://img.icons8.com/color/96/dumbbell.png', 'Personal Care'),

    -- EXPENSE: Misc
    ('Gifts & Donations','EXPENSE','#C97D63','https://img.icons8.com/color/96/gift.png', NULL),
    ('Taxes',         'EXPENSE', '#8E8E93', 'https://img.icons8.com/color/96/tax.png', NULL),
    ('Fees & Charges','EXPENSE', '#8E8E93', 'https://img.icons8.com/color/96/receipt.png', NULL),
    ('Pets',          'EXPENSE', '#6FC2B0', 'https://img.icons8.com/color/96/dog.png', NULL),

    -- INCOME
    ('Income',        'INCOME',  '#27AE60', 'https://img.icons8.com/color/96/money-bag.png', NULL),
    ('Salary',        'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money.png', 'Income'),
    ('Bonus',         'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money-transfer.png', 'Income'),
    ('Interest',      'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/interest-rate.png', 'Income'),
    ('Dividends',     'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/stocks.png', 'Income'),
    ('Refunds & Reimbursements','INCOME','#56D27E','https://img.icons8.com/color/96/refund-2.png','Income'),
    ('Other Income',  'INCOME',  '#56D27E', 'https://img.icons8.com/color/96/money-bag-euro.png', 'Income');

-----------------------------------------------------------------------
-- 2) Upsert top-level categories
-----------------------------------------------------------------------
INSERT INTO t_category (
    id, user_id, name, color, category_type, icon_url,
    parent_category_id, created_at, updated_at, created_by, updated_by, version
)
SELECT
    gen_random_uuid(),
    'system',
    d.name,
    d.color,
    d.category_type,
    d.icon_url,
    NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    'system', 'system',
    0
FROM _default_categories d
WHERE d.parent_name IS NULL
ON CONFLICT (user_id, name) DO NOTHING;

-----------------------------------------------------------------------
-- 3) Upsert child categories
-----------------------------------------------------------------------
INSERT INTO t_category (
    id, user_id, name, color, category_type, icon_url,
    parent_category_id, created_at, updated_at, created_by, updated_by, version
)
SELECT
    gen_random_uuid(),
    'system',
    d.name,
    d.color,
    d.category_type,
    d.icon_url,
    p.id AS parent_category_id,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
    'system', 'system',
    0
FROM _default_categories d
JOIN t_category p
  ON p.user_id = 'system'
 AND p.name    = d.parent_name
WHERE d.parent_name IS NOT NULL
ON CONFLICT (user_id, name) DO NOTHING;

COMMIT;
