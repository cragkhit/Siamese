    public void modifyDecisionInstruction(int id, int condition, String frameSlot, String linkName, int objectId, String attribute, int positive, int negative) throws FidoDatabaseException, ObjectNotFoundException, InstructionNotFoundException {
        try {
            Connection conn = null;
            Statement stmt = null;
            try {
                if ((condition == ConditionalOperatorTable.CONTAINS_LINK) || (condition == ConditionalOperatorTable.NOT_CONTAINS_LINK)) {
                    ObjectTable ot = new ObjectTable();
                    if (ot.contains(objectId) == false) throw new ObjectNotFoundException(objectId);
                }
                conn = fido.util.FidoDataSource.getConnection();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                if (contains(stmt, positive) == false) throw new InstructionNotFoundException(positive);
                if (contains(stmt, negative) == false) throw new InstructionNotFoundException(negative);
                String sql = "update Instructions set Operator = " + condition + ", " + "                        FrameSlot = '" + frameSlot + "', " + "                        LinkName = '" + linkName + "', " + "                        ObjectId = " + objectId + ", " + "                        AttributeName = '" + attribute + "' " + "where InstructionId = " + id;
                stmt.executeUpdate(sql);
                InstructionGroupTable groupTable = new InstructionGroupTable();
                groupTable.deleteInstruction(stmt, id);
                if (positive != -1) groupTable.addInstructionAt(stmt, id, 1, positive);
                if (negative != -1) groupTable.addInstructionAt(stmt, id, 2, negative);
                conn.commit();
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException e) {
            throw new FidoDatabaseException(e);
        }
    }
