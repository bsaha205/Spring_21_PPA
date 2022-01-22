module Interpreter where
import Data.List

data Expr = 
   Var Name --- a variable
   | App Expr Expr --- function application
   | Lambda Name Expr --- lambda abstraction
   deriving
   (Eq,Show)   --- the Expr data type derives from built-in Eq and Show classes,
			--- thus, we can compare and print expressions

type Name = String --- a variable name


freeVars :: Expr -> [Name]
freeVars expr = case expr of
	Var x -> [x]
	App expr1 expr2 -> freeVars(expr1) `union` freeVars(expr2)
	Lambda x expr1 -> freeVars(expr1) \\ [x]

concatenation:: Name -> Name
concatenation x = x ++ "_"

-- freshVars (Lambda "1_" (App (Var "x") (App (Var "1_") (Var "2_"))))
freshVars :: Expr -> [Name]
freshVars expr = let x = map show [1..300] in
    map concatenation x \\ freeVars(expr)

subst :: (Name, Expr) -> Expr -> Expr
subst (x, expr1) expr2 =  case expr2 of
	Var y -> if x == y then expr1 
		else expr2
	App expr3 expr4 -> App (subst (x, expr1) expr3) (subst (x, expr1) expr4)
	Lambda y expr3 -> if x == y then Lambda y expr3 
		else Lambda z (subst (x, expr1) (subst (y, Var z) expr3))
		where z = head(freshVars(expr3) `intersect` freshVars(expr1) `intersect` freshVars(Var x))


normNF_OneStep :: Expr -> Maybe Expr
normNF_OneStep expr = case expr of
	Var x -> Nothing
	App expr1 expr2 -> case expr1 of
	   Lambda y expr3 -> Just (subst (y, expr2) expr3)
	   _ -> let expr4 = normNF_OneStep expr1 in case expr4 of
	    	Nothing -> let expr5 = normNF_OneStep expr2 in case expr5 of 
	    		Nothing -> Nothing
	    		Just expr6 -> Just (App expr1 expr6)
	    	Just expr7 -> Just (App expr7 expr2)
	Lambda y expr1 -> let expr2 = normNF_OneStep expr1 in case expr2 of
		Nothing -> Nothing
		Just expr3 -> Just (Lambda y expr3)


normNF :: Int -> Expr -> Expr
normNF 0 expr = expr
normNF n expr = let expr1 = (normNF_OneStep expr) in case expr1 of
	Nothing -> expr
	Just expr2 -> normNF (n-1) expr2




-- normNF_OneStep :: Expr -> Maybe Expr
-- normNF_OneStep expr = case expr of
-- 	Var x -> Nothing
-- 	Lambda y expr1 -> let expr2 = normNF_OneStep expr1 in case expr2 of
-- 		Nothing -> Nothing
-- 		Just expr3 -> Just (Lambda y expr3)
-- 	App expr1 expr2 -> case expr1 of
-- 	   Lambda z expr3 -> Just (subst (z, expr2) expr1)
-- 	   _ -> let expr4 = normNF_OneStep expr1 in case expr4 of
-- 	    	Nothing -> let expr5 = normNF_OneStep expr2 in case expr5 of 
-- 	    		Nothing -> Nothing
-- 	    		Just expr6 -> Just (App expr1 expr6)
-- 	    	Just expr7 -> Just (App expr7 expr2)






-- normNF_OneStep (App (App (Lambda "x" (Var "x")) (Lambda "x" (Var "x"))) (App (Lambda "x" (Var "x")) (Lambda "x" (Var "x"))))

-- normNF_OneStep (App (App (Var "x") (App (Var "y") (App (Lambda "x" (Var "x")) (Lambda "x" (Var "x"))))) (App (Lambda "x" (Var "x")) (Lambda "x" (Var "x"))))

-- normNF 100 (App (Lambda "x" (App (Var "x") (Var "x"))) (Lambda "x" (App (Var "x") (Var "x"))))

--  normNF 1 (App (Lambda "y" (Var "z")) (App (Lambda "x" (App (Var "x") (Var "x"))) (Lambda "x" (App (Var "x") (Var "x")))))

--  normNF 1 (App (Lambda "x" (Lambda "y" (Var "x"))) (Lambda "z" (App (App (Lambda "x" (Lambda "y" (Var "x"))) (Var "z")) (App (Lambda "x" (App (Var "z") (Var "x"))) (Lambda "x" (App (Var "z") (Var "x")))))))

--  normNF 2 (App (Lambda "x" (Lambda "y" (Var "x"))) (Lambda "z" (App (App (Lambda "x" (Lambda "y" (Var "x"))) (Var "z")) (App (Lambda "x" (App (Var "z") (Var "x"))) (Lambda "x" (App (Var "z") (Var "x")))))))

--  normNF 3 (App (Lambda "x" (Lambda "y" (Var "x"))) (Lambda "z" (App (App (Lambda "x" (Lambda "y" (Var "x"))) (Var "z")) (App (Lambda "x" (App (Var "z") (Var "x"))) (Lambda "x" (App (Var "z") (Var "x")))))))