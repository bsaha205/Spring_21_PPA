module Utils
  ( ToImplement(..)
  , addConstraint, addConstraints
  , newTVar, substVar, substAll, applySubst, addTEnv
  , occursIn
  , unifySet  )
  where
import Control.Exception
import Data.Typeable
import Data

-- Errors.
data ToImplement = ToImplement deriving (Show, Typeable)
instance Exception ToImplement

addConstraint :: ConstraintEnv -> Type -> Type -> ConstraintEnv
addConstraint cenv expected inferred =
  addConstraints cenv [(expected, inferred)]
  
  
addConstraints :: ConstraintEnv -> [(Type, Type)] -> ConstraintEnv
addConstraints cenv new =
  CEnv { constraints = new ++ (constraints cenv)
       , var = var cenv
       , tenv = tenv cenv }

{- Generates a fresh type variable. -}
newTVar :: ConstraintEnv -> (Type, ConstraintEnv)
newTVar cenv = let cenv' = CEnv { constraints = constraints cenv
                   , var = var cenv + 1
                   , tenv = tenv cenv }
  in (TVar ("__" ++ show (var cenv)), cenv')
  
  
  {--Creates a new env with new variable mapping added--}
addTEnv :: ConstraintEnv -> Ident -> (Type, ConstraintEnv)
addTEnv cenv v =
  let cenv' = CEnv { constraints = constraints cenv
                   , var = var cenv + 1
                   , tenv = tenv cenv ++ [(v,TVar ("__" ++ show (var cenv)))] }
  in (TVar ("__" ++ show (var cenv)),cenv')

{- Substitution. -}
substVar from to haystack@(TBase _) = haystack
substVar from to var@(TVar _) = if var == from then to else var
substVar from to var@(TArrow t1 t2) =
  TArrow (substVar from to t1) (substVar from to t2)

substAll :: Type -> Type -> Constraints -> Constraints
substAll from to = map substPair
  where substPair (x, y) = (substVar from to x, substVar from to y)

applySubst :: Type -> Subst -> Type
applySubst = foldl doSubst
  where doSubst inType (from, to) = substVar (TVar from) to inType

applySubstList :: Constraints -> Subst -> Constraints
applySubstList c s = if c == [] then []
                     else
                     let c1 = [(applySubst (fst (head c)) s, applySubst (snd (head c)) s)]
                     in c1 ++ applySubstList (tail c) s

occursIn :: Type -> Type -> Bool
occursIn needle (TBase _) = False
occursIn needle haystack@(TVar _) = needle == haystack
occursIn needle (TArrow t1 t2) = (occursIn needle t1) || (occursIn needle t2)

{--Unify one constraint--}
unify :: (Type,Type) -> Subst
unify (t1,TVar tvar) = if occursIn t1 (TVar tvar) == False then [(tvar, t1)] else throw TypeCircularity
unify (TVar tvar,t2) = if occursIn t2 (TVar tvar) == False then [(tvar, t2)] else throw TypeCircularity
unify (TBase b1, TBase b2) = if b1 == b2 then [] else throw (TypeMismatch (TBase b1) (TBase b2))
unify (TArrow t11 t12, TArrow t21 t22) = let s1 = unify (t11,t21)
                                         in
                                         let s2 = unify ((applySubst t12 s1),(applySubst t22 s1))
                                         in
                                         s1 ++ s2
{--unify (_,_) = throw TypeCircularity--}


{--Unify a set of constraints--}
unifySet :: Constraints -> Subst
unifySet c = if c == [] then []
             else
             let c2 = head c
             in
             let s = unify ((fst c2),(snd c2))
             in
             s ++ (unifySet (applySubstList (tail c) s))
